package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.SadSamTraceabilityLinkRecoveryEvaluation;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.TraceLinkEvaluationIT;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagramsWithTLR;

public class TraceLinkEvaluationERID extends TraceLinkEvaluationIT<GoldStandardDiagramsWithTLR> {
    protected static String OUTPUT = TraceLinkEvaluationIT.OUTPUT;

    @EnabledIfEnvironmentVariable(named = "useDiagramRecognitionMock", matches = ".*")
    @DisplayName("Evaluate SAD-SAM (Mock)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(20)
    protected void evaluateSadSamTlrITMock(GoldStandardDiagramsWithTLR project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluationERID(true);
        var results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @DisabledIfEnvironmentVariable(named = "useDiagramRecognitionMock", matches = ".*")
    @DisplayName("Evaluate SAD-SAM")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(20)
    @Override
    protected void evaluateSadSamTlrIT(GoldStandardDiagramsWithTLR project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluationERID(false);
        var results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @EnabledIfEnvironmentVariable(named = "useDiagramRecognitionMock", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluate TLR (Historical) (Mock)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(21)
    protected void evaluateSadSamTlrHistoricalITMock(GoldStandardDiagramsWithTLR project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluationERID(true);
        ArDoCoResult arDoCoResult = evaluation.getArDoCoResult(project);
        Assertions.assertNotNull(arDoCoResult);

        SadSamTraceabilityLinkRecoveryEvaluation.checkResults(project, arDoCoResult);
        SadSamTraceabilityLinkRecoveryEvaluation.writeDetailedOutput(project, arDoCoResult);
    }

    @DisabledIfEnvironmentVariable(named = "useDiagramRecognitionMock", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluate TLR (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(21)
    @Override
    protected void evaluateSadSamTlrHistoricalIT(GoldStandardDiagramsWithTLR project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluationERID(false);
        ArDoCoResult arDoCoResult = evaluation.getArDoCoResult(project);
        Assertions.assertNotNull(arDoCoResult);

        SadSamTraceabilityLinkRecoveryEvaluation.checkResults(project, arDoCoResult);
        SadSamTraceabilityLinkRecoveryEvaluation.writeDetailedOutput(project, arDoCoResult);
    }

    @Override
    @Disabled
    protected void evaluateSadSamCodeTlrIT(CodeProject codeProject) {
    }

    @Override
    @Disabled
    protected void evaluateSamCodeTlrFullIT(CodeProject project) {
    }

    @Override
    @Disabled
    protected void compareSadSamTlRForPcmAndUmlIT(Project project) {
    }

    @Override
    @Disabled
    protected void evaluateSamCodeTlrIT(CodeProject project) {
    }
}
