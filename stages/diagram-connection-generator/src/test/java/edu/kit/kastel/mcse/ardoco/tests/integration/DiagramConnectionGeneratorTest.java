package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.tests.TestRunner;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramConnectionGeneratorTest {
    private static final Logger logger = LoggerFactory.getLogger(DiagramConnectionGeneratorTest.class);
    private static final String OUTPUT_DIR = "src/test/resources/testout";

    @DisplayName("Evaluate Diagram Connection Generator")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    void evaluateNonHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    @DisplayName("Evaluate Diagram Connection Generator (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    void evaluateHistoricalDiagramRecognition(DiagramProject project) {
        run(project);
    }

    @Test
    void teamstoreTest() {
        run(DiagramProject.TEAMMATES);
    }

    private void run(DiagramProject project) {
        logger.info("Evaluate Diagram Connection for {}", project.name());
        var runner = new TestRunner(project.name());
        var params = new TestRunner.Parameters(project, new File(OUTPUT_DIR), true);

        runner.setUp(params);
        runner.runWithoutSaving();

        var dataRepository = runner.getArDoCo().getDataRepository();
        var text = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).get().getText();
        var diagramRecognition = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).get();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).get();
        var diagrams = diagramRecognition.getDiagrams();
        //TODO Get Metamodel properly
        var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(project.getMetamodel());
        var diagramLinks = diagramConnectionState.getDiagramLinks();
        var traceLinks = diagramConnectionState.getTraceLinks().stream().peek(t -> t.setText(text)).sorted().toList();
        var goldStandardTraceLinks = project.getDiagramTextTraceLinksFromGoldstandard().stream().peek(t -> t.setText(text)).sorted().toList();

        var totalSentences = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).get().getText().getSentences().size();
        var totalDiagramElements = diagrams.stream().flatMap(d -> d.getBoxes().stream()).toList().size();
        var total = totalSentences * totalDiagramElements;
        //TODO
        var tpLinks = goldStandardTraceLinks.stream().filter(g -> traceLinks.contains(g)).sorted().toList();
        var TP = tpLinks.size();
        var fpLinks = traceLinks.stream().filter(t -> !goldStandardTraceLinks.contains(t)).sorted().toList();
        var FP = fpLinks.size();
        var fnLinks = goldStandardTraceLinks.stream().filter(g -> !traceLinks.contains(g)).sorted().toList();
        var FN = fnLinks.size();
        var TN = total - TP - FP - FN;
        var P = TP / (double) (TP + FP);
        var R = TP / (double) (TP + FN);
        var acc = (TP + TN) / (double) (TP + TN + FP + FN);
        logger.info("TP:{}, FP:{}, TN:{}, FN:{}, P:{}, R:{}, Acc:{}", TP, FP, TN, FN, P, R, acc);
    }
}
