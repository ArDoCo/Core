/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramConsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedDiagram;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.PackageSelection;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.RefactoringBundle;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Rename;

class Stage1SyntheticDiagramTest extends SyntheticTestBase {
    private static Result calculateResult(Diagram syntheticDiagram, DiagramMatchingModelSelectionState selectionState,
            MutableBiMap<String, String> expectedLinksInArchitecture, MutableBiMap<String, String> expectedLinksInCode, GeneralModelType selected,
            boolean selectionIsArchitecture, Decision expected) {
        int countInArchitecture = 0;
        int countInCode = 0;

        int unnecessaryLinksInArchitecture = 0;
        int unnecessaryLinksInCode = 0;

        for (var box : syntheticDiagram.getBoxes()) {
            List<DiagramMatchingModelSelectionState.Occurrence> occurrencesInArchitecture = selectionState.getOccurrences(box.getUUID(),
                    ArchitectureModelType.UML);
            if (!occurrencesInArchitecture.isEmpty()) {
                countInArchitecture++;

                var expectedLink = expectedLinksInArchitecture.get(box.getUUID());

                for (var occurrence : occurrencesInArchitecture) {
                    if (!occurrence.modelID().equals(expectedLink)) {
                        unnecessaryLinksInArchitecture++;
                    }
                }
            }
            List<DiagramMatchingModelSelectionState.Occurrence> occurrencesInCode = selectionState.getOccurrences(box.getUUID(), CodeModelType.CODE_MODEL);
            if (!occurrencesInCode.isEmpty()) {
                countInCode++;

                var expectedLink = expectedLinksInCode.get(box.getUUID());

                for (var occurrence : occurrencesInCode) {
                    if (!occurrence.modelID().equals(expectedLink)) {
                        unnecessaryLinksInCode++;
                    }
                }
            }
        }

        double ratioInArchitecture = (double) countInArchitecture / syntheticDiagram.getBoxes().size();
        double ratioInCode = (double) countInCode / syntheticDiagram.getBoxes().size();

        double percentageOfUnnecessaryLinksInArchitecture = (double) unnecessaryLinksInArchitecture / countInArchitecture;
        double percentageOfUnnecessaryLinksInCode = (double) unnecessaryLinksInCode / countInCode;

        Decision actual = new Decision(selected, selectionIsArchitecture ? ratioInArchitecture : ratioInCode, selectionIsArchitecture ?
                percentageOfUnnecessaryLinksInArchitecture :
                percentageOfUnnecessaryLinksInCode);

        return new Result(expected, actual, selectionIsArchitecture ? ratioInCode : ratioInArchitecture);
    }

    @DisplayName("Examine the stage 1 using synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineStage1Base(DiagramProject project) throws IOException {
        Consumer<Result> printer = (result) -> {
            double difference = result.actual().ratio() - result.otherRatio();
            try {
                this.writer.write(String.format("%s: r: %s, u: %s, d: %s%n", result.expected().type(), result.actual().ratio(), result.actual()
                        .percentageOfUnnecessaryLinks(), difference));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };

        printer.accept(this.examineStage1(project, GeneralModelType.ARCHITECTURE));
        printer.accept(this.examineStage1(project, GeneralModelType.CODE));
    }

    private Result examineStage1(DiagramProject project, GeneralModelType generalModelType) throws IOException {
        String name = project.name().toLowerCase(Locale.ROOT);
        File inputArchitectureModel = project.getSourceProject().getModelFile(ArchitectureModelType.UML);
        File inputCodeModel = new File(Objects.requireNonNull(project.getSourceProject().getCodeModelDirectory())).getAbsoluteFile();
        File inputDiagram = File.createTempFile("temp", ".json");
        File outputDir = new File(PIPELINE_OUTPUT);

        inputDiagram.deleteOnExit();
        var preparedDiagram = this.prepareDiagram(project, generalModelType);
        if (preparedDiagram == null) {
            return null;
        }
        Diagram syntheticDiagram = preparedDiagram.getFirst();
        MutableBiMap<String, String> expectedLinks = preparedDiagram.getSecond();

        Decision expected = new Decision(generalModelType, (double) expectedLinks.size() / syntheticDiagram.getBoxes().size(), 0.0);

        createObjectMapper().writeValue(inputDiagram, syntheticDiagram);

        DiagramConsistency runner = new DiagramConsistency(name);

        SortedMap<String, String> config = new TreeMap<>();
        runner.setUp(inputArchitectureModel, inputCodeModel, inputDiagram, outputDir, config);
        runner.run();

        ModelStates models = runner.getDataRepository().getData(ModelStates.ID, ModelStates.class).orElseThrow();
        DiagramMatchingModelSelectionState selectionState = runner.getDataRepository()
                .getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionStateImpl.class)
                .orElseThrow();

        if (selectionState.getSelection() == null) {
            return new Result(expected, null, 0.0);
        }

        boolean selectionIsArchitecture = selectionState.getSelection().contains(ArchitectureModelType.UML);
        GeneralModelType selected = selectionIsArchitecture ? GeneralModelType.ARCHITECTURE : GeneralModelType.CODE;

        MutableBiMap<String, String> expectedLinksInArchitecture = selectionIsArchitecture ? expectedLinks : new HashBiMap<>();
        MutableBiMap<String, String> expectedLinksInCode = selectionIsArchitecture ? new HashBiMap<>() : expectedLinks;

        return calculateResult(syntheticDiagram, selectionState, expectedLinksInArchitecture, expectedLinksInCode, selected, selectionIsArchitecture, expected);
    }

    private Pair<Diagram, MutableBiMap<String, String>> prepareDiagram(DiagramProject project, GeneralModelType generalModelType) {
        switch (generalModelType) {
        case ARCHITECTURE -> {
            AnnotatedDiagram<ArchitectureItem> diagram = getAnnotatedArchitectureDiagram(project);

            diagram = applyRefactoring(diagram, new RefactoringBundle<>(Map.of(new Rename<>(), (int) (REFACTORING_RATIO * diagram.diagram()
                    .getBoxes()
                    .size()))));
            if (diagram == null) {
                return null;
            }

            return Pair.of(diagram.diagram(), diagram.getIdBasedLinks(ModelElement::getId));
        }
        case CODE -> {
            AnnotatedDiagram<CodeItem> diagram = getAnnotatedCodeDiagram(project);

            diagram = applyRefactoring(diagram, new PackageSelection<>());
            if (diagram == null) {
                return null;
            }

            diagram = applyRefactoring(diagram, new RefactoringBundle<>(Map.of(new Rename<>(), (int) (REFACTORING_RATIO * diagram.diagram()
                    .getBoxes()
                    .size()))));
            if (diagram == null) {
                return null;
            }

            return Pair.of(diagram.diagram(), diagram.getIdBasedLinks(ModelElement::getId));
        }
        default -> throw new IllegalArgumentException("Unexpected value: " + generalModelType);
        }
    }

    record Decision(GeneralModelType type, double ratio, double percentageOfUnnecessaryLinks) {

    }

    record Result(Decision expected, Decision actual, double otherRatio) {
    }
}
