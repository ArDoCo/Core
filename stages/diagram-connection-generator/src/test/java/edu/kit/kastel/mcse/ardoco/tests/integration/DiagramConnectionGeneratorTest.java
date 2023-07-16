package edu.kit.kastel.mcse.ardoco.tests.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tests.PreTestRunner;
import edu.kit.kastel.mcse.ardoco.tests.TestRunner;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.results.Results;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiagramConnectionGeneratorTest extends StageTest<DiagramConnectionGenerator, Results> {
    private static final Logger logger = LoggerFactory.getLogger(DiagramConnectionGeneratorTest.class);
    private static final String OUTPUT_DIR = "src/test/resources/testout";

    public DiagramConnectionGeneratorTest() {
        super(new DiagramConnectionGenerator(null));
    }

    @Override
    protected Results runComparable(DiagramProject project, Map<String, String> additionalConfigurations) {
        var dataRepository = run(project, additionalConfigurations);
        var text = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
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
        logger.info("{} Diagram Links, {} Trace Links, {} Most Specific Trace Links", diagramLinks.size(), traceLinks.size(), mostSpecificTraceLinks.size());
        logger.info(result.toString());
        logger.info("Alt: " + altResult);
        //assertTrue(altResult.asExpected());
        return altResult;
    }

    @Override
    protected DataRepository runPreTestRunner(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        var runner = new PreTestRunner(project.name());
        var params = new PreTestRunner.Parameters(project, new File(OUTPUT_DIR), true);

        runner.setUp(params);
        runner.runWithoutSaving();

        return runner.getArDoCo().getDataRepository();
    }

    @Override
    protected DataRepository runTestRunner(DiagramProject project, Map<String, String> additionalConfigurations, DataRepository dataRepository) {
        logger.info("Run TestRunner for {}", project.name());
        var runner = new TestRunner(project.name());
        var params = new TestRunner.Parameters(project, new File(OUTPUT_DIR), true, additionalConfigurations);

        runner.getArDoCo().getDataRepository().addAllData(dataRepository);

        runner.setUp(params);
        runner.runWithoutSaving();

        return runner.getArDoCo().getDataRepository();
    }

    @DisplayName("Evaluate Diagram Connection Generator")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    void evaluateNonHistoricalDiagramRecognition(DiagramProject project) {
        assertTrue(runComparable(project).asExpected());
    }

    @DisplayName("Evaluate Diagram Connection Generator (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    void evaluateHistoricalDiagramRecognition(DiagramProject project) {
        assertTrue(runComparable(project).asExpected());
    }

    @Test
    void teammatesTest() {
        runComparable(DiagramProject.TEAMMATES);
    }

    @Test
    void teastoreTest() {
        runComparable(DiagramProject.TEASTORE);
    }

    @Test
    void bbbTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON);
    }

    @Test
    void msTest() {
        runComparable(DiagramProject.MEDIASTORE);
    }
}
