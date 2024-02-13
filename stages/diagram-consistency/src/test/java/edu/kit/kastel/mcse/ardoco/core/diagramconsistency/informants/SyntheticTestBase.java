/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;
import static org.apache.commons.lang3.ClassUtils.getSimpleName;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramConsistency;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.EvaluationTestBase;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.Metrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.MetricsStats;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedDiagram;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Connect;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Create;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Delete;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Disconnect;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Move;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Refactoring;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.RefactoringBundle;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Rename;

class SyntheticTestBase extends EvaluationTestBase {
    protected static final int REFACTORING_TYPE_COUNT = 6;

    protected AnnotatedDiagram<ArchitectureItem> getAnnotatedAndRefactoredArchitectureDiagram(ExaminationDescription description) {
        AnnotatedDiagram<ArchitectureItem> diagram = getAnnotatedArchitectureDiagram(description.project());

        if (description.archPreRefactoring() != null) {
            diagram = applyRefactoring(diagram, description.archPreRefactoring());
            if (diagram == null) {
                return null;
            }
        }

        if (description.refactoringIndex() != -1) {
            diagram = this.applyRefactoring(description.refactoringIndex(), diagram);
        }
        return diagram;
    }

    protected AnnotatedDiagram<CodeItem> getAnnotatedAndRefactoredCodeDiagram(ExaminationDescription description) {
        AnnotatedDiagram<CodeItem> diagram = getAnnotatedCodeDiagram(description.project());

        if (description.codePreRefactoring() != null) {
            diagram = applyRefactoring(diagram, description.codePreRefactoring());
            if (diagram == null) {
                return null;
            }
        }

        if (description.refactoringIndex() != -1) {
            diagram = this.applyRefactoring(description.refactoringIndex(), diagram);
        }
        return diagram;
    }

    protected <R, M> Refactoring<R, M> selectRefactoring(int index) {
        return switch (index % REFACTORING_TYPE_COUNT) {
        case -1 -> null;
        case +0 -> new Connect<>();
        case +1 -> new Create<>();
        case +2 -> new Delete<>();
        case +3 -> new Disconnect<>();
        case +4 -> new Move<>();
        case +5 -> new Rename<>();
        default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }

    protected <M> AnnotatedDiagram<M> applyRefactoring(int index, AnnotatedDiagram<M> diagram) {
        int size = diagram.diagram().getBoxes().size();
        int count = (int) (REFACTORING_RATIO * size);

        Refactoring<Box, M> refactoring = this.selectRefactoring(index);
        return applyRefactoring(diagram, new RefactoringBundle<>(Map.of(refactoring, count)));
    }

    protected void doEvaluationIterations(ExaminationDescription description, int iterationsPerCase) throws IOException {
        MetricsStats stats = new MetricsStats();

        int iterations = iterationsPerCase;

        if (description.project() == null) {
            iterations *= getDistinctDiagrams().size();
        }

        for (int i = 0; i < iterations; i++) {
            DiagramProject usedProject = description.project();
            if (usedProject == null) {
                usedProject = getDistinctDiagrams().get(i % getDistinctDiagrams().size());
            }

            Metrics metrics = this.examineOnSyntheticDiagram(description.withProject(usedProject));
            if (metrics == null) {
                continue;
            }

            stats.add(metrics, 1.0);
        }

        assertNotEquals(0, stats.getCount());

        Object preProcessing = description.generalModelType() == GeneralModelType.ARCHITECTURE ?
                description.archPreRefactoring() :
                description.codePreRefactoring();

        this.writer.write(String.format(
                "#### Run on project '%s', refactoring '%s', model type '%s', epsilon '%f', max iterations '%d', levenshtein '%f', pre-process '%s', (%d/%d) runs ####\n",
                Optional.ofNullable(description.project()).map(DiagramProject::name).orElse("all"), getSimpleName(this.selectRefactoring(description
                        .refactoringIndex())), description.generalModelType().name(), description.epsilon(), description.maxAlgorithmIterations(), description
                                .textSimilarityThreshold(), getSimpleName(preProcessing), stats.getCount(), iterations));

        this.writer.write(String.format("Average precision: %.2f\n", stats.getAveragePrecision()));
        this.writer.write(String.format("Average recall: %.2f\n", stats.getAverageRecall()));
        this.writer.write(String.format("Average F1: %.2f\n", stats.getAverageF1Score()));
    }

    private Metrics examineOnSyntheticDiagram(ExaminationDescription description) throws IOException {
        String name = description.project().name().toLowerCase(Locale.ROOT);
        File inputArchitectureModel = description.project().getSourceProject().getModelFile(ArchitectureModelType.UML);
        File inputCodeModel = new File(Objects.requireNonNull(description.project().getSourceProject().getCodeModelDirectory())).getAbsoluteFile();
        File inputDiagram = File.createTempFile("temp", ".json");
        File outputDir = new File(PIPELINE_OUTPUT);

        inputDiagram.deleteOnExit();
        AnnotatedDiagram<?> syntheticDiagram;
        ModelType modelType;

        switch (description.generalModelType()) {
        case ARCHITECTURE -> {
            AnnotatedDiagram<ArchitectureItem> diagram = this.getAnnotatedAndRefactoredArchitectureDiagram(description);
            if (diagram == null)
                return null;

            syntheticDiagram = diagram;
            modelType = ArchitectureModelType.UML;
        }
        case CODE -> {
            AnnotatedDiagram<CodeItem> diagram = this.getAnnotatedAndRefactoredCodeDiagram(description);
            if (diagram == null)
                return null;

            syntheticDiagram = diagram;
            modelType = CodeModelType.CODE_MODEL;
        }
        default -> throw new IllegalArgumentException("Unexpected value: " + description.generalModelType());
        }

        createObjectMapper().writeValue(inputDiagram, syntheticDiagram.diagram());

        DiagramConsistency runner = new DiagramConsistency(name);
        this.setupExamination(modelType, runner.getDataRepository());

        SortedMap<String, String> config = new TreeMap<>();
        config.put("DiagramModelLinkInformant::epsilon", String.valueOf(description.epsilon()));
        config.put("DiagramModelLinkInformant::maxIterations", String.valueOf(description.maxAlgorithmIterations()));
        config.put("DiagramModelLinkInformant::textSimilarityThreshold", String.valueOf(description.textSimilarityThreshold()));
        config.put("DiagramModelLinkInformant::similarityThreshold", String.valueOf(0.0));

        runner.setUp(inputArchitectureModel, inputCodeModel, inputDiagram, outputDir, config);
        runner.run();

        return this.getResultsOfExamination(syntheticDiagram, modelType, runner.getDataRepository());
    }

    protected void setupExamination(ModelType modelType, DataRepository data) {

    }

    protected Metrics getResultsOfExamination(AnnotatedDiagram<?> diagram, ModelType modelType, DataRepository data) {
        return null;
    }
}
