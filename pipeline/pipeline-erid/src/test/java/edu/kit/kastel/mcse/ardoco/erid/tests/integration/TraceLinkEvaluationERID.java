package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.SadSamTraceabilityLinkRecoveryEvaluation;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.TraceLinkEvaluationIT;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagramsWithTLR;

public class TraceLinkEvaluationERID extends TraceLinkEvaluationIT<GoldStandardDiagramsWithTLR> {
    protected static String OUTPUT = TraceLinkEvaluationIT.OUTPUT;

    @DisplayName("Evaluate SAD-SAM TLR")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(20)
    @Override
    protected void evaluateSadSamTlrIT(GoldStandardDiagramsWithTLR project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluationERID();
        var results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluate TLR (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getHistoricalProjects")
    @Order(21)
    protected void evaluateSadSamTlrHistoricalIT(GoldStandardDiagramsWithTLR project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluationERID();
        ArDoCoResult arDoCoResult = evaluation.getArDoCoResult(project);
        Assertions.assertNotNull(arDoCoResult);

        SadSamTraceabilityLinkRecoveryEvaluation.checkResults(project, arDoCoResult);
        SadSamTraceabilityLinkRecoveryEvaluation.writeDetailedOutput(project, arDoCoResult);
    }
}
