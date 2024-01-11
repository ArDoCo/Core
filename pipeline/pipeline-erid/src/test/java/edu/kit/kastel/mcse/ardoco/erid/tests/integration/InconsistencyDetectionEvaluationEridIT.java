/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.InconsistencyDetectionEvaluationIT;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagramsWithTLR;

/**
 * Performs the inconsistency detection using ERID with the diagram recognition.
 */
public class InconsistencyDetectionEvaluationEridIT extends InconsistencyDetectionEvaluationIT {
    @Override
    protected HoldBackRunResultsProducer getHoldBackRunResultsProducer() {
        return new HoldBackRunResultsProducerERID(false);
    }

    @DisplayName("Evaluating MME-Inconsistency Detection")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    @Override
    protected void missingModelElementInconsistencyIT(GoldStandardProject goldStandardProject) {
        if (goldStandardProject instanceof GoldStandardDiagramsWithTLR goldStandardDiagramProject)
            runMissingModelElementInconsistencyEval(goldStandardProject, goldStandardDiagramProject.getExpectedMMEResults());
        else
            throw new IllegalArgumentException("Invalid method source");
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection (Historic)")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    @Override
    protected void missingModelElementInconsistencyHistoricIT(GoldStandardProject goldStandardProject) {
        if (goldStandardProject instanceof GoldStandardDiagramsWithTLR goldStandardDiagramProject)
            runMissingModelElementInconsistencyEval(goldStandardProject, goldStandardDiagramProject.getExpectedMMEResults());
        else
            throw new IllegalArgumentException("Invalid method source");
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(5)
    @Override
    protected void missingModelElementInconsistencyBaselineIT(GoldStandardProject goldStandardProject) {
        super.missingModelElementInconsistencyBaselineIT(goldStandardProject);
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline (Historical)")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(6)
    @Override
    protected void missingModelElementInconsistencyBaselineHistoricIT(GoldStandardProject goldStandardProject) {
        super.missingModelElementInconsistencyBaselineHistoricIT(goldStandardProject);
    }

    /**
     * Tests the inconsistency detection for undocumented model elements on all {@link Project projects}.
     *
     * @param goldStandardProject Project that gets inserted automatically with the enum {@link Project}.
     */
    @DisplayName("Evaluate Inconsistency Analyses For MissingTextForModelElementInconsistencies")
    @ParameterizedTest(name = "Evaluating UME-inconsistency for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(10)
    @Override
    protected void missingTextInconsistencyIT(GoldStandardProject goldStandardProject) {
        super.missingTextInconsistencyIT(goldStandardProject);
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluate Inconsistency Analyses For MissingTextForModelElementInconsistencies (Historical)")
    @ParameterizedTest(name = "Evaluating UME-inconsistency for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(11)
    @Override
    protected void missingTextInconsistencyHistoricIT(GoldStandardProject goldStandardProject) {
        super.missingTextInconsistencyHistoricIT(goldStandardProject);
    }
}
