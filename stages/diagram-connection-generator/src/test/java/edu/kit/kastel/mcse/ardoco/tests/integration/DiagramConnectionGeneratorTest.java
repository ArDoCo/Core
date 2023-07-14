package edu.kit.kastel.mcse.ardoco.tests.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tests.PreTestRunner;
import edu.kit.kastel.mcse.ardoco.tests.TestRunner;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.results.Results;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramConnectionGeneratorTest {
    private static final Logger logger = LoggerFactory.getLogger(DiagramConnectionGeneratorTest.class);
    private static final String OUTPUT_DIR = "src/test/resources/testout";
    private static Map<DiagramProject, DataRepository> preRun = new HashMap<>();

    private DataRepository setup(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        var runner = new PreTestRunner(project.name());
        var params = new PreTestRunner.Parameters(project, new File(OUTPUT_DIR), true);

        runner.setUp(params);
        runner.runWithoutSaving();

        var dataRepository = runner.getArDoCo().getDataRepository();
        return dataRepository;
    }

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

    private Results run(DiagramProject project) {
        logger.info("Evaluate Diagram Connection for {}", project.name());
        var runner = new TestRunner(project.name());
        var params = new TestRunner.Parameters(project, new File(OUTPUT_DIR), true);

        var dataRepository = runner.getArDoCo().getDataRepository();
        dataRepository.addAllData(preRun.computeIfAbsent(project, this::setup));

        runner.setUp(params);
        runner.runWithoutSaving();

        var text = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).get().getText();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).get();
        //TODO Get Metamodel properly
        var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(project.getMetamodel());
        var diagramLinks = diagramConnectionState.getDiagramLinks().stream().sorted().collect(Collectors.toCollection(TreeSet::new));
        var traceLinks = diagramConnectionState.getTraceLinks().stream().peek(t -> t.setText(text)).collect(Collectors.toCollection(TreeSet::new));
        var mostSpecificTraceLinks = diagramConnectionState.getMostSpecificTraceLinks()
                .stream()
                .peek(t -> t.setText(text))
                .collect(Collectors.toCollection(TreeSet::new));
        var result = Results.create(project, text, traceLinks, project.getExpectedDiagramTraceLinkResults());
        var altResult = Results.create(project, text, mostSpecificTraceLinks, project.getExpectedDiagramTraceLinkResults());
        logger.info(result.toString());
        logger.info("Alt: " + altResult);
        //assertTrue(altResult.asExpected());
        return altResult;
    }

    @Configurable
    private static final int repetitions = 3;
    private static final Results[] results = new Results[repetitions];

    @RepeatedTest(repetitions)
    void repetitionTest(RepetitionInfo repetitionInfo) {
        results[repetitionInfo.getCurrentRepetition() - 1] = run(DiagramProject.TEAMMATES);
        if (repetitionInfo.getCurrentRepetition() == repetitionInfo.getTotalRepetitions()) {
            for (var i = 0; i < results.length - 1; i++) {
                assertEquals(results[i], results[i + 1]);
            }
        }
    }
}
