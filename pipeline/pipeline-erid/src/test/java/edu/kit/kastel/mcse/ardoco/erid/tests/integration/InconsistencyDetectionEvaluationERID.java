package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.InconsistencyDetectionEvaluationIT;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class InconsistencyDetectionEvaluationERID extends InconsistencyDetectionEvaluationIT {
    private static List<Project> getNonHistoricalProjects() {
        return DiagramProject.getNonHistoricalProjects().stream().map(DiagramProject::getBaseProject).toList();
    }

    private static List<Project> getHistoricalProjects() {
        return DiagramProject.getHistoricalProjects().stream().map(DiagramProject::getBaseProject).toList();
    }

    @Override
    protected HoldBackRunResultsProducer getHoldBackRunResultsProducer() {
        return new HoldBackRunResultsProducerERID();
    }

    @DisplayName("Evaluating MME-Inconsistency Detection")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @MethodSource("getNonHistoricalProjects")
    @Order(1)
    @Override
    protected void missingModelElementInconsistencyIT(Project project) {
        runMissingModelElementInconsistencyEval(project);
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection (Historic)")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @MethodSource("getHistoricalProjects")
    @Order(2)
    @Override
    protected void missingModelElementInconsistencyHistoricIT(Project project) {
        runMissingModelElementInconsistencyEval(project);
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @MethodSource("getNonHistoricalProjects")
    @Order(5)
    @Override
    protected void missingModelElementInconsistencyBaselineIT(Project project) {
        runMissingModelElementInconsistencyBaselineEval(project);
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline (Historical)")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @MethodSource("getHistoricalProjects")
    @Order(6)
    @Override
    protected void missingModelElementInconsistencyBaselineHistoricIT(Project project) {
        runMissingModelElementInconsistencyBaselineEval(project);
    }
}
