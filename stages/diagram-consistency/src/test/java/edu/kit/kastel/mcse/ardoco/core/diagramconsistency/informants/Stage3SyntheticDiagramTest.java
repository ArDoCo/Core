/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.MapMetrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.Metrics;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedDiagram;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Mixed;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.PartialSelection;

class Stage3SyntheticDiagramTest extends SyntheticTestBase {
    @DisplayName("Examine stage 3 using synthetic diagrams")
    @Test
    @Disabled("Only for manual testing")
    void examineBaseOfStage3OnSyntheticDiagram() throws IOException {
        assertNotEquals(0, GeneralModelType.values().length);
        int iterationsPerCase = 2;
        for (GeneralModelType generalModelType : GeneralModelType.values()) {
            for (int refactoringIndex = 0; refactoringIndex < REFACTORING_TYPE_COUNT; refactoringIndex++) {
                this.doEvaluationIterations(new ExaminationDescriptionBuilder().setProject(null)
                        .setGeneralModelType(generalModelType)
                        .setRefactoringIndex(refactoringIndex)
                        .createEvaluationDescription(), iterationsPerCase);
            }
        }
    }

    @DisplayName("Examine stage 3 using partial synthetic code diagrams")
    @Test
    @Disabled("Only for manual testing")
    void examineStage3OnPartialSyntheticDiagram() throws IOException {
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

    @DisplayName("Examine stage 3 using mixed refactorings on synthetic code diagrams")
    @Test
    @Disabled("Only for manual testing")
    void examineStage3OnMixedRefactoredDiagram() throws IOException {
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

    @Override
    protected Metrics getResultsOfExamination(AnnotatedDiagram<?> diagram, ModelType modelType, DataRepository data) {
        List<Inconsistency<String, String>> found = data.getData(DiagramModelInconsistencyState.ID, DiagramModelInconsistencyState.class)
                .orElseThrow()
                .getInconsistencies(modelType);

        List<Inconsistency<String, String>> expected = diagram.inconsistencies().stream().map(inconsistency -> inconsistency.map(Box::getUUID, entity -> {
            if (entity instanceof CodeItem codeItem) {
                return Extractions.getPath(codeItem);
            } else if (entity instanceof Entity item) {
                return item.getId();
            }

            return null;
        })).toList();

        return MapMetrics.from(getMapBasedInconsistencySet(expected), getMapBasedInconsistencySet(found));
    }
}
