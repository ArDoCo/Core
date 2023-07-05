package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.File;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import edu.kit.kastel.mcse.ardoco.tests.TestRunner;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.results.Results;

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
    void teammatesTest() {
        run(DiagramProject.TEAMMATES);
    }

    @Test
    void teastoreTest() {
        run(DiagramProject.TEASTORE);
    }

    @Test
    void bbbTest() {
        run(DiagramProject.BIGBLUEBUTTON);
    }

    @Test
    void msTest() {
        run(DiagramProject.MEDIASTORE);
    }

    private void run(DiagramProject project) {
        logger.info("Evaluate Diagram Connection for {}", project.name());
        var runner = new TestRunner(project.name());
        var params = new TestRunner.Parameters(project, new File(OUTPUT_DIR), true);

        runner.setUp(params);
        runner.runWithoutSaving();

        var dataRepository = runner.getArDoCo().getDataRepository();
        var text = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).get().getText();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).get();
        //TODO Get Metamodel properly
        var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(project.getMetamodel());
        var diagramLinks = diagramConnectionState.getDiagramLinks().stream().sorted().collect(Collectors.toCollection(TreeSet::new));
        var traceLinks = diagramConnectionState.getTraceLinks().stream().peek(t -> t.setText(text)).collect(Collectors.toCollection(TreeSet::new));
        var result = Results.create(project, text, traceLinks, project.getExpectedDiagramTraceLinkResults());
        logger.info(result.toString());
        assert (result.asExpected());
    }
}
