/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelLinkState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.MapMetrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.Metrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedDiagram;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.AppendSuffix;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Mixed;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.PartialSelection;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.RefactoringBundle;

class Stage2SyntheticDiagramTest extends SyntheticTestBase {
    @DisplayName("Examine stage 2 using synthetic diagrams")
    @Test
    @Disabled("Only for manual testing")
    void examineBaseOnSyntheticDiagram() throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        int iterationsPerCase = 1;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
                this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(null)
                        .setGeneralModelType(generalModelType)
                        .setRefactoringIndex(refactoringIndex)
                        .createEvaluationDescription(), iterationsPerCase);
            }
        }
    }

    @DisplayName("Examine stage 2 using partial synthetic code diagrams")
    @Test
    @Disabled("Only for manual testing")
    void examineStage2OnPartialSyntheticDiagram() throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        int iterationsPerCase = 2;
        for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
            this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(null)
                    .setGeneralModelType(GeneralModelType.CODE)
                    .setRefactoringIndex(refactoringIndex)
                    .setCodePreRefactoring(new PartialSelection<>(PARTIAL_SELECTION_MIN, PARTIAL_SELECTION_MAX))
                    .createEvaluationDescription(), iterationsPerCase);
        }
    }

    @DisplayName("Examine stage 2 using mixed refactorings on synthetic code diagrams")
    @Test
    @Disabled("Only for manual testing")
    void examineStage2OnMixedRefactoredDiagram() throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(null)
                    .setGeneralModelType(generalModelType)
                    .setRefactoringIndex(-1)
                    .setArchPreRefactoring(new Mixed<>(REFACTORING_RATIO, null))
                    .setCodePreRefactoring(new Mixed<>(REFACTORING_RATIO, null))
                    .createEvaluationDescription(), 1);
        }
    }

    @DisplayName("Examine the impact of iteration count on stage 2 using synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineIterationCountOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        int iterationsPerCase = 5;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
                for (int maxIterations = 0; maxIterations < 2; maxIterations++) {
                    this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                            .setGeneralModelType(generalModelType)
                            .setRefactoringIndex(refactoringIndex)
                            .setMaxAlgorithmIterations(maxIterations)
                            .createEvaluationDescription(), iterationsPerCase);
                }
            }
        }
    }

    @DisplayName("Examine the impact of iteration count on stage 2 using (partial) synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineIterationCountPartialOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Boolean> booleans = List.of(false, true);
        int iterationsPerCase = 10;
        for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
            for (var usePartial : booleans) {
                for (int maxIterations = 0; maxIterations < 2; maxIterations++) {
                    this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                            .setGeneralModelType(GeneralModelType.CODE)
                            .setRefactoringIndex(refactoringIndex)
                            .setMaxAlgorithmIterations(maxIterations)
                            .setCodePreRefactoring(usePartial ? new PartialSelection<>(PARTIAL_SELECTION_MIN, PARTIAL_SELECTION_MAX) : null)
                            .createEvaluationDescription(), iterationsPerCase);
                }
            }
        }
    }

    @DisplayName("Examine the impact of the epsilon on stage 2 using synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineEpsilonOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Double> epsilons = List.of(2.0, 1.5, 1.0, 0.75, 0.5, 0.25);
        int iterationsPerCase = 5;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
                for (var epsilon : epsilons) {
                    this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                            .setGeneralModelType(generalModelType)
                            .setRefactoringIndex(refactoringIndex)
                            .setEpsilon(epsilon)
                            .createEvaluationDescription(), iterationsPerCase);
                }
            }
        }
    }

    @DisplayName("Examine the impact of the (limited range) epsilon on stage 2 using synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineEpsilon2OnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Double> epsilons = List.of(1.25, 1.125, 1.0, 0.8, 0.65, 0.5);
        int iterationsPerCase = 5;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
                for (var epsilon : epsilons) {
                    this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                            .setGeneralModelType(generalModelType)
                            .setRefactoringIndex(refactoringIndex)
                            .setEpsilon(epsilon)
                            .createEvaluationDescription(), iterationsPerCase);
                }
            }
        }
    }

    @DisplayName("Examine the impact of the (limited range) epsilon on stage 2 using (partial) synthetic diagrams and mixed refactorings")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineEpsilonPartial2MixedOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Double> epsilons = List.of(1.25, 1.125, 1.0, 0.8, 0.65, 0.5);
        int iterationsPerCase = 50;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (var epsilon : epsilons) {
                this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                        .setGeneralModelType(generalModelType)
                        .setEpsilon(epsilon)
                        .setArchPreRefactoring(new Mixed<>(REFACTORING_RATIO, null))
                        .setCodePreRefactoring(new Mixed<>(REFACTORING_RATIO, new PartialSelection<>(PARTIAL_SELECTION_MIN, PARTIAL_SELECTION_MAX)))
                        .createEvaluationDescription(), iterationsPerCase);
            }
        }
    }

    @DisplayName("Examine the impact of the (limited range) epsilon on stage 2 using (partial) synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineEpsilon2PartialOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Double> epsilons = List.of(1.25, 1.125, 1.0, 0.8, 0.65, 0.5);
        int iterationsPerCase = 10;
        for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
            for (var epsilon : epsilons) {
                this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                        .setGeneralModelType(GeneralModelType.CODE)
                        .setRefactoringIndex(refactoringIndex)
                        .setEpsilon(epsilon)
                        .setCodePreRefactoring(new PartialSelection<>(PARTIAL_SELECTION_MIN, PARTIAL_SELECTION_MAX))
                        .createEvaluationDescription(), iterationsPerCase);
            }
        }
    }

    @DisplayName("Examine the impact of the levenshtein threshold on stage 2 using synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineLevenshteinOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Double> thresholds = List.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5);
        int iterationsPerCase = 5;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
                for (var threshold : thresholds) {
                    this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                            .setGeneralModelType(generalModelType)
                            .setRefactoringIndex(refactoringIndex)
                            .setTextSimilarityThreshold(threshold)
                            .createEvaluationDescription(), iterationsPerCase);
                }
            }
        }
    }

    @DisplayName("Examine the impact of the levenshtein threshold on stage 2 using (partial) synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineLevenshteinPartialOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Double> thresholds = List.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5);
        int iterationsPerCase = 10;
        for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
            for (var threshold : thresholds) {
                this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                        .setGeneralModelType(GeneralModelType.CODE)
                        .setRefactoringIndex(refactoringIndex)
                        .setTextSimilarityThreshold(threshold)
                        .setCodePreRefactoring(new PartialSelection<>(PARTIAL_SELECTION_MIN, PARTIAL_SELECTION_MAX))
                        .createEvaluationDescription(), iterationsPerCase);
            }
        }
    }

    @DisplayName("Examine the impact of only using parts on stage 2 using synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examinePartialOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Boolean> booleans = List.of(false, true);
        int iterationsPerCase = 10;
        for (int refactoringIndex = -1; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
            for (var usePartial : booleans) {
                this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                        .setGeneralModelType(GeneralModelType.CODE)
                        .setRefactoringIndex(refactoringIndex)
                        .setCodePreRefactoring(usePartial ? new PartialSelection<>(PARTIAL_SELECTION_MIN, PARTIAL_SELECTION_MAX) : null)
                        .createEvaluationDescription(), iterationsPerCase);
            }
        }
    }

    @DisplayName("Examine the impact of a suffix on stage 2 using synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineSuffixOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Boolean> booleans = List.of(false, true);
        int iterationsPerCase = 5;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
                for (var useSuffix : booleans) {
                    this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                            .setGeneralModelType(generalModelType)
                            .setRefactoringIndex(refactoringIndex)
                            .setArchPreRefactoring(useSuffix ? new AppendSuffix<>("Impl") : null)
                            .setCodePreRefactoring(useSuffix ? new AppendSuffix<>("Impl") : null)
                            .createEvaluationDescription(), iterationsPerCase);
                }
            }
        }
    }

    @DisplayName("Examine the impact of a suffix on stage 2 using partial synthetic diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDistinctDiagrams")
    @Disabled("Only for manual testing")
    void examineSuffixAndPartialOnSyntheticDiagram(DiagramProject project) throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        List<Boolean> booleans = List.of(false, true);
        int iterationsPerCase = 10;
        for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
            for (var useSuffixAndPartial : booleans) {
                this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(project)
                        .setGeneralModelType(GeneralModelType.CODE)
                        .setRefactoringIndex(refactoringIndex)
                        .setCodePreRefactoring(useSuffixAndPartial ?
                                new RefactoringBundle<>(Map.of(new AppendSuffix<>("Impl"), 1, new PartialSelection<>(PARTIAL_SELECTION_MIN,
                                        PARTIAL_SELECTION_MAX), 1)) :
                                null)
                        .createEvaluationDescription(), iterationsPerCase);
            }
        }
    }

    @Override
    protected void setupExamination(ModelType modelType, DataRepository data) {
        DiagramMatchingModelSelectionStateImpl modelSelection = new DiagramMatchingModelSelectionStateImpl();
        modelSelection.setSelection(Set.of(modelType));
        data.addData(DiagramMatchingModelSelectionState.ID, modelSelection);
    }

    @Override
    protected Metrics getResultsOfExamination(AnnotatedDiagram<?> diagram, ModelType modelType, DataRepository data) {
        Optional<DiagramModelLinkState> matchingState = data.getData(DiagramModelLinkState.ID, DiagramModelLinkState.class);
        assertTrue(matchingState.isPresent());

        Optional<ModelStates> models = data.getData(ModelStates.ID, ModelStates.class);
        assertTrue(models.isPresent());

        MutableBiMap<String, String> foundLinks = matchingState.get().getLinks(modelType);
        MutableBiMap<String, String> expectedLinks = diagram.getIdBasedLinks(modelType == CodeModelType.CODE_MODEL ?
                element -> Extractions.getPath((CodeItem) element) :
                element -> ((ArchitectureItem) element).getId());

        return MapMetrics.from(expectedLinks, foundLinks);
    }
}
