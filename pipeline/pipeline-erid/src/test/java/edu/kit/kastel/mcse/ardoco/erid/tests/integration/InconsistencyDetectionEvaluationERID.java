package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.InconsistencyDetectionEvaluationIT;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;

public class InconsistencyDetectionEvaluationERID extends InconsistencyDetectionEvaluationIT {
    @Override
    protected HoldBackRunResultsProducer getHoldBackRunResultsProducer() {
        return new HoldBackRunResultsProducerERID();
    }

    @DisplayName("Evaluating MME-Inconsistency Detection")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    @Override
    protected void missingModelElementInconsistencyIT(GoldStandardProject goldStandardProject) {
        runMissingModelElementInconsistencyEval(goldStandardProject);
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection (Historic)")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    @Override
    protected void missingModelElementInconsistencyHistoricIT(GoldStandardProject goldStandardProject) {
        runMissingModelElementInconsistencyEval(goldStandardProject);
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(5)
    @Override
    protected void missingModelElementInconsistencyBaselineIT(GoldStandardProject goldStandardProject) {
        runMissingModelElementInconsistencyBaselineEval(goldStandardProject);
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline (Historical)")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(6)
    @Override
    protected void missingModelElementInconsistencyBaselineHistoricIT(GoldStandardProject goldStandardProject) {
        runMissingModelElementInconsistencyBaselineEval(goldStandardProject);
    }
}
