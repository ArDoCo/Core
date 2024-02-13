/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tests.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.SortedMaps;
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
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.tests.PreTestRunner;
import edu.kit.kastel.mcse.ardoco.tests.Results;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest;

/**
 * This class is used for evaluating ERID's diagram-to-sentences TLR capabilities using the manually extracted diagrams
 * ({@link edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock DiagramRecognitionMock}).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiagramConnectionGeneratorTest extends StageTest<DiagramConnectionGenerator, DiagramProject, Results> {
    private static final Logger logger = LoggerFactory.getLogger(DiagramConnectionGeneratorTest.class);
    private static final String OUTPUT_DIR = "src/test/resources/testout";

    public DiagramConnectionGeneratorTest() {
        super(new DiagramConnectionGenerator(SortedMaps.mutable.empty(), new DataRepository()), DiagramProject.values());
    }

    protected ExpectedResults getExpectedResults(DiagramProject project) {
        return project.getExpectedDiagramSentenceTlrResultsWithMock();
    }

    @Override
    protected Results runComparable(DiagramProject project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun) {
        var dataRepository = run(project, additionalConfigurations, cachePreRun);
        var text = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(project.getMetamodel());
        var linksBetweenDeAndRi = diagramConnectionState.getLinksBetweenDeAndRi().stream().sorted().collect(Collectors.toCollection(TreeSet::new));
        var traceLinks = new TreeSet<>(diagramConnectionState.getTraceLinks());
        var mostSpecificTraceLinks = new TreeSet<>(diagramConnectionState.getMostSpecificTraceLinks());
        var globalConfiguration = dataRepository.getGlobalConfiguration();
        var altResult = Results.create(globalConfiguration, project, text, mostSpecificTraceLinks, getExpectedResults(project));

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

        logger.debug(
                "{} Diagram Links, {} Trace Links, {} Most Specific Trace Links, {} Common Noun " + "FP, " + "{} Shared Stem FP, {} Other Entity FP, {} Coreference FN",
                linksBetweenDeAndRi.size(), traceLinks.size(), mostSpecificTraceLinks.size(), commonNoun.size(), sharedStem.size(), otherEntity.size(),
                coreference.size());

        var cacheID = "Results-" + project.name();
        var prevResults = getCached(cacheID, Results.class);

        if (prevResults != null) {
            var dAll = Results.difference(globalConfiguration, altResult.all(), prevResults.all());
            var dTP = Results.difference(globalConfiguration, altResult.truePositives(), prevResults.truePositives());
            var dFP = Results.difference(globalConfiguration, altResult.falsePositives(), prevResults.falsePositives());
            var dFN = Results.difference(globalConfiguration, altResult.falseNegatives(), prevResults.falseNegatives());
            var dTN = Results.difference(globalConfiguration, prevResults.falsePositives(), altResult.falsePositives());
        }

        if (!altResult.equalsByConfusionMatrix(prevResults))
            debugCacheIfCachingFlag(cacheID, altResult);

        return altResult;
    }

    @Override
    protected DataRepository runPreTestRunner(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        var params = new PreTestRunner.Parameters(project, new File(OUTPUT_DIR), true);
        var runner = new PreTestRunner(project.name(), params);
        return runner.runWithoutSaving();
    }

    @Override
    protected DataRepository runTestRunner(DiagramProject project, SortedMap<String, String> additionalConfigurations, DataRepository preRunDataRepository) {
        logger.info("Run TestRunner for {}", project.name());
        var combinedConfigs = new TreeMap<>(project.getAdditionalConfigurations());
        combinedConfigs.putAll(additionalConfigurations);
        return new AnonymousRunner(project.name(), preRunDataRepository) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                dataRepository.getGlobalConfiguration().getWordSimUtils().setConsiderAbbreviations(true);
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                pipelineSteps.add(new DiagramConnectionGenerator(combinedConfigs, dataRepository));
                return pipelineSteps;
            }
        }.runWithoutSaving();
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
        var avg = new LinkedHashMap<String, Double>();
        var avgWeighted = new LinkedHashMap<String, Double>();
        var totalGoldStandardPositives = 0L;
        for (var result : results) {
            totalGoldStandardPositives += result.goldStandardPositives();
        }
        for (var result : results) {
            var weight = result.goldStandardPositives() / (double) totalGoldStandardPositives;
            var metrics = result.mapOfMetrics();
            for (var metric : metrics.entrySet()) {
                var key = metric.getKey();
                var value = metric.getValue();
                var weightedByGoldStandardAndAmountOfProjects = value * weight;
                var weightedByAmountOfProjects = value / projects.size();
                var oldAvg = avg.getOrDefault(key, 0.0);
                var oldWeighted = avgWeighted.getOrDefault(key, 0.0);
                avg.put(key, oldAvg + weightedByAmountOfProjects);
                avgWeighted.put(key, oldWeighted + weightedByGoldStandardAndAmountOfProjects);
            }
        }
        System.out.println("Project & " + String.join(" & ", avg.keySet()) + "\\\\");
        results.stream()
                .map(r -> r.project().getAlias() + " & " + r.mapOfMetrics()
                        .values()
                        .stream()
                        .map(d -> String.format(Locale.ENGLISH, "%.2f", d))
                        .collect(Collectors.joining(" & ")) + "\\\\")
                .forEach(System.out::println);
        System.out.println("Average & " + avg.values().stream().map(d -> String.format(Locale.ENGLISH, "%.2f", d)).collect(Collectors.joining(" & ")) + "\\\\");
        System.out.println("w. Average & " + avgWeighted.values()
                .stream()
                .map(d -> String.format(Locale.ENGLISH, "%.2f", d))
                .collect(Collectors.joining(" & ")) + "\\\\");
    }

    @Disabled
    @Test
    void teammatesTest() {
        runComparable(DiagramProject.TEAMMATES, false);
    }

    @Disabled
    @Test
    void teammatesHistTest() {
        runComparable(DiagramProject.TEAMMATES_HISTORICAL, false);
    }

    @Disabled
    @Test
    void teastoreTest() {
        runComparable(DiagramProject.TEASTORE, false);
    }

    @Disabled
    @Test
    void teastoreHistTest() {
        runComparable(DiagramProject.TEASTORE_HISTORICAL, false);
    }

    @Disabled
    @Test
    void bbbTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON, false);
    }

    @Disabled
    @Test
    void bbbHistTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON_HISTORICAL, false);
    }

    @Disabled
    @Test
    void msTest() {
        runComparable(DiagramProject.MEDIASTORE, false);
    }
}
