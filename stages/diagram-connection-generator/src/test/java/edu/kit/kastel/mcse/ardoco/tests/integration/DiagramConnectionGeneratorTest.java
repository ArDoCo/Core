package edu.kit.kastel.mcse.ardoco.tests.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
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
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
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
        super(new DiagramConnectionGenerator(Map.of(), null));
    }

    @Override
    protected Results runComparable(DiagramProject project, Map<String, String> additionalConfigurations) {
        var dataRepository = run(project, additionalConfigurations);
        var text = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        //TODO Get Metamodel properly
        var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(project.getMetamodel());
        var diagramLinks = diagramConnectionState.getDiagramLinks().stream().sorted().collect(Collectors.toCollection(TreeSet::new));
        var traceLinks = diagramConnectionState.getTraceLinks().stream().collect(Collectors.toCollection(TreeSet::new));
        var mostSpecificTraceLinks = diagramConnectionState.getMostSpecificTraceLinks().stream().collect(Collectors.toCollection(TreeSet::new));
        var altResult = Results.create(project, text, mostSpecificTraceLinks, project.getExpectedDiagramTraceLinkResults());

        var commonNoun = altResult.falsePositives().stream().filter(w -> {
            var related = w.getRelatedGSLinks();
            return !related.isEmpty() && related.stream().allMatch(g -> g.getTraceType().equals(TraceType.COMMON_NOUN));
        }).toList();
        var sharedStem = altResult.falsePositives().stream().filter(w -> {
            var related = w.getRelatedGSLinks();
            return !related.isEmpty() && related.stream().allMatch(g -> g.getTraceType().equals(TraceType.SHARED_STEM));
        }).toList();
        var otherEntity = altResult.falsePositives().stream().filter(w -> {
            var related = w.getRelatedGSLinks();
            return !related.isEmpty() && related.stream().allMatch(g -> g.getTraceType().equals(TraceType.OTHER_ENTITY));
        }).toList();
        var coreference = altResult.falseNegatives().stream().filter(w -> w.getTraceType().equals(TraceType.ENTITY_COREFERENCE)).toList();

        logger.info(
                "{} Diagram Links, {} Trace Links, {} Most Specific Trace Links, {} Common Noun FP, {} Shared Stem FP, {} Other Entity FP, {} Coreference FN",
                diagramLinks.size(), traceLinks.size(), mostSpecificTraceLinks.size(), commonNoun.size(), sharedStem.size(), otherEntity.size(),
                coreference.size());
        logger.info(altResult.toString());

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

    @Disabled
    @Test
    void evaluateAll() {
        var projects = new ArrayList<>(DiagramProject.getNonHistoricalProjects());
        projects.addAll(DiagramProject.getHistoricalProjects());
        var results = new ArrayList<Results>();
        for (var project : projects) {
            results.add(runComparable(project));
        }
        var avg = new double[7];
        var avgWeighted = new double[7];
        var totalGoldStandardPositives = 0;
        for (var result : results) {
            totalGoldStandardPositives += result.GS_P();
            System.out.println(result.project().name() + " & " + result.toTableRow() + "\\\\");
        }
        for (var result : results) {
            var weight = result.GS_P() / (double) totalGoldStandardPositives;
            var metrics = result.rawMetrics();
            for (var i = 0; i < metrics.length; i++) {
                avg[i] += metrics[i] / projects.size();
                avgWeighted[i] += metrics[i] * weight;
            }
        }
        System.out.println("Average & " + Arrays.stream(avg)
                .map(d -> Math.round(d * 100.0) / 100.0)
                .<String>mapToObj(Double::toString)
                .collect(Collectors.joining(" & ")) + "\\\\");
        System.out.println("w. Average & " + Arrays.stream(avgWeighted)
                .map(d -> Math.round(d * 100.0) / 100.0)
                .<String>mapToObj(Double::toString)
                .collect(Collectors.joining(" & ")) + "\\\\");
    }

    @Test
    void teammatesTest() {
        runComparable(DiagramProject.TEAMMATES);
    }

    @Test
    void teammatesHistTest() {
        runComparable(DiagramProject.TEAMMATES_HISTORICAL);
    }

    @Test
    void teastoreTest() {
        runComparable(DiagramProject.TEASTORE);
    }

    @Test
    void teastoreHistTest() {
        runComparable(DiagramProject.TEASTORE_HISTORICAL);
    }

    @Test
    void bbbTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON);
    }

    @Test
    void bbbHistTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON_HISTORICAL);
    }

    @Test
    void msTest() {
        runComparable(DiagramProject.MEDIASTORE);
    }
}
