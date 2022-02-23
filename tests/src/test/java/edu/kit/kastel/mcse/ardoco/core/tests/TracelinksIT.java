/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.files.*;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class TracelinksIT {
    private static Logger logger = null;

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String ADDITIONAL_CONFIG = null;
    private static final List<TLProjectEvalResult> RESULTS = new ArrayList<>();
    private static final Map<Project, AgentDatastructure> DATA_MAP = new HashMap<>();

    private File inputText;
    private File inputModel;
    private File additionalConfigs = null;
    private File outputDir = new File(OUTPUT);

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("log4j.configurationFile", "src/main/resources/log4j2.xml");
        logger = LogManager.getLogger(TracelinksIT.class);
    }

    @AfterAll
    public static void afterAll() throws IOException {
        var evalDir = Path.of(OUTPUT).resolve("tl_eval");
        Files.createDirectories(evalDir);

        TLSummaryFile.save(evalDir.resolve("summary.md"), RESULTS, DATA_MAP);
        TLModelFile.save(evalDir.resolve("models.md"), DATA_MAP);
        TLSentenceFile.save(evalDir.resolve("sentences.md"), DATA_MAP);
        TLLogFile.append(evalDir.resolve("log.md"), RESULTS);
        TLPreviousFile.save(evalDir.resolve("previous.csv"), RESULTS); // save before loading to guarantee file exists
        TLDiffFile.save(evalDir.resolve("diff.md"), RESULTS, TLPreviousFile.load(evalDir.resolve("previous.csv")), DATA_MAP);
    }

    @BeforeEach
    void beforeEach() {
        // set the cache to true (default setting)
        // if another tests does not want to have a cache they can manipulate themselves
        OntologyTextProvider.enableCache(true);
    }

    @AfterEach
    void afterEach() {
        if (ADDITIONAL_CONFIG != null) {
            File config = new File(ADDITIONAL_CONFIG);
            config.delete();
        }
        if (additionalConfigs != null) {
            additionalConfigs = null;
        }
    }

    // NOTE: if you only want to test a specific project, you can simply set up the EnumSource
    // For more details, see https://www.baeldung.com/parameterized-tests-junit-5#3-enum
    // Example: add ", names = { "BIGBLUEBUTTON" }" to EnumSource
    // However, make sure to revert this before you commit and push!
    @DisplayName("Evaluate TLR (Ontology-based)")
    @ParameterizedTest(name = "Evaluating {0} (Onto)")
    @EnumSource(value = Project.class)
    void compareTraceLinksIT(Project project) {
        compareOntologyBased(project);
    }

    @Disabled("Disabled for CI. Enable for local test only!")
    @DisplayName("Evaluate TLR (Text-based)")
    @ParameterizedTest(name = "Evaluating {0} (Text)")
    @EnumSource(value = Project.class)
    void compareTraceLinksTextIT(Project project) {
        compareTextBased(project);
    }

    private void compareOntologyBased(Project project) {
        inputText = null;
        inputModel = project.getTextOntologyFile();

        compare(project);
    }

    private void compareTextBased(Project project) {
        inputText = project.getTextFile();
        inputModel = project.getModelFile();

        compare(project);
    }

    private void compare(Project project) {
        var name = project.name().toLowerCase();
        var data = Pipeline.runAndSave("test_" + name, inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(data);

        var results = calculateResults(name, data);
        var expectedResults = project.getExpectedTraceLinkResults();

        if (logger.isInfoEnabled()) {
            String infoString = String.format(Locale.ENGLISH,
                    "\n%s:\n\tPrecision:\t%.3f (min. expected: %.3f)%n\tRecall:\t\t%.3f (min. expected: %.3f)%n\tF1:\t\t%.3f (min. expected: %.3f)", name,
                    results.getPrecision(), expectedResults.getPrecision(), results.getRecall(), expectedResults.getRecall(), results.getF1(),
                    expectedResults.getF1());
            logger.info(infoString);

            logger.debug("False negatives:\n{}", results.getFalseNegative().stream().map(Object::toString).collect(Collectors.joining("\n")));
            logger.debug("False positives:\n{}", results.getFalsePositives().stream().map(Object::toString).collect(Collectors.joining("\n")));
        }

        try {
            RESULTS.add(new TLProjectEvalResult(project, data));
            DATA_MAP.put(project, data);
        } catch (IOException e) {
            e.printStackTrace(); // failing to save project results is irrelevant for test success
        }

        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(),
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(),
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(),
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.getF1()));

    }

    private EvaluationResults calculateResults(String name, AgentDatastructure data) {
        var connectionState = data.getConnectionState();
        Set<String> traceLinks = getTraceLinksFromConnectionState(connectionState);
        logger.info("Found {} trace links", traceLinks.size());

        var goldStandard = getGoldStandard(name);

        var results = TestUtil.compare(traceLinks, goldStandard);
        return results;
    }

    private Set<String> getTraceLinksFromConnectionState(IConnectionState connectionState) {
        var formatString = "%s,%d";
        return connectionState.getTraceLinks().collect(tl -> String.format(formatString, tl.getModelElementUid(), tl.getSentenceNumber() + 1)).castToSet();
    }

    private List<String> getGoldStandard(String name) {
        var pathStr = String.format("src/test/resources/%s/goldstandard.csv", name);
        Path path = Paths.get(pathStr);
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove(0);
        return goldLinks;
    }

    @Disabled("Disabled for CI. Only enable this locally if you want to test the cache.")
    @DisplayName("Compare cached and non-cached evaluation")
    @ParameterizedTest
    @ValueSource(strings = { "mediastore", "teastore", "teammates" })
    void compareCachingResultsIT(String name) {
        // set up
        var similarity = 1.0;
        if (name.equals("teammates")) {
            similarity = 0.8;
        }
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, TestUtil.getSimilarityConfigString(similarity));

        var inputFilePath = String.format("src/test/resources/%s/%s_w_text.owl", name, name);
        inputModel = new File(inputFilePath);

        var runName = "test_" + name;

        // run cached
        OntologyTextProvider.enableCache(true);
        var dataCached = Pipeline.runAndSave(runName + "_cached", inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(dataCached);
        var resultsCached = calculateResults(name, dataCached);
        logger.info("Cached results for {}:\n{}", name, resultsCached.toPrettyString());

        // run not cached
        OntologyTextProvider.enableCache(false);
        var dataNonCached = Pipeline.runAndSave(runName, inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(dataNonCached);
        var resultsNonCached = calculateResults(name, dataNonCached);
        logger.info("Non-cached results for {}:\n{}", name, resultsNonCached.toPrettyString());

        Assertions.assertEquals(resultsNonCached, resultsCached, "Results for cached and non-cached run are not equal");
    }
}
