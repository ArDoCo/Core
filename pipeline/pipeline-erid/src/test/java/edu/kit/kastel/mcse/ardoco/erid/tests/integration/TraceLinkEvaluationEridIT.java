/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.TraceLinkEvaluationIT;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagramsWithTLR;

/**
 * Performs the SAD SAM TLR using ERID with the diagram recognition.
 */
public class TraceLinkEvaluationEridIT extends TraceLinkEvaluationIT<GoldStandardDiagramsWithTLR> {
    protected static String OUTPUT = TraceLinkEvaluationIT.OUTPUT;

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

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluate TLR (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(21)
    @Override
    protected void evaluateSadSamTlrHistoricalIT(GoldStandardDiagramsWithTLR project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluationERID(false);
        var results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @Override
    @Disabled
    protected void evaluateSadSamCodeTlrIT(CodeProject codeProject) {
    }

    @Override
    @Disabled
    protected void evaluateSadSamCodeTlrFullIT(CodeProject project) {
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
