package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;

class TracelinksIT {

    private static final double SIMILARITY_MS = 1.0;
    private static final double MIN_F1_MS = 0.52d;
    private static final double MIN_PREC_MS = 0.46d;
    private static final double MIN_RECALL_MS = 0.6d;

    private static final double SIMILARITY_TM = 0.80;
    private static final double MIN_F1_TM = 0.77d;
    private static final double MIN_PREC_TM = 0.68d;
    private static final double MIN_RECALL_TM = 0.88d;

    private static final double SIMILARITY_TS = 1.0;
    private static final double MIN_F1_TS = 0.82d;
    private static final double MIN_PREC_TS = 0.78d;
    private static final double MIN_REC_TS = 0.87d;

    private static final Logger logger = LogManager.getLogger(TracelinksIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String ADDITIONAL_CONFIG = "src/test/resources/config.properties";

    private File inputText;
    private File inputModel;
    private File additionalConfigs = new File(ADDITIONAL_CONFIG);
    private File outputDir = new File(OUTPUT);

    @BeforeEach
    void beforeEach() {
        // set the cache to true (default setting)
        // if another tests does not want to have a cache they can manipulate themselves
        OntologyTextProvider.enableCache(true);
    }

    @AfterEach
    void afterEach() {
        File config = new File(ADDITIONAL_CONFIG);
        config.delete();
    }

    @Test
    @DisplayName("Evaluate Teastore")
    void compareTracelinksTeastoreIT() {
        var similarity = SIMILARITY_TS;
        var minPrecision = MIN_PREC_TS;
        var minRecall = MIN_REC_TS;
        var minF1 = MIN_F1_TS;

        compareOntologyBased("teastore", similarity, minPrecision, minRecall, minF1);
    }

    @Disabled("Disabled for CI. Enable for local test only!")
    @Test
    @DisplayName("Evaluate Teastore (Text)")
    void compareTracelinksTeastoreTextIT() {
        var similarity = SIMILARITY_TS;
        var minPrecision = MIN_PREC_TS;
        var minRecall = MIN_REC_TS;
        var minF1 = MIN_F1_TS;

        compareTextBased("teastore", similarity, minPrecision, minRecall, minF1);
    }

    @Test
    @DisplayName("Evaluate Teammates")
    void compareTracelinksTeammatesIT() {
        var similarity = SIMILARITY_TM;
        var minPrecision = MIN_PREC_TM;
        var minRecall = MIN_RECALL_TM;
        var minF1 = MIN_F1_TM;

        compareOntologyBased("teammates", similarity, minPrecision, minRecall, minF1);
    }

    @Disabled("Disabled for CI. Enable for local test only!")
    @Test
    @DisplayName("Evaluate Teammates (Text)")
    void compareTracelinksTeammatesTextIT() {
        var similarity = SIMILARITY_TM;
        var minPrecision = MIN_PREC_TM;
        var minRecall = MIN_RECALL_TM;
        var minF1 = MIN_F1_TM;

        compareTextBased("teammates", similarity, minPrecision, minRecall, minF1);
    }

    @Test
    @DisplayName("Evaluate Mediastore")
    void compareTracelinksMediastoreIT() {
        var similarity = SIMILARITY_MS;
        var minPrecision = MIN_PREC_MS;
        var minRecall = MIN_RECALL_MS;
        var minF1 = MIN_F1_MS;

        compareOntologyBased("mediastore", similarity, minPrecision, minRecall, minF1);
    }

    @Disabled("Disabled for CI. Enable for local test only!")
    @Test
    @DisplayName("Evaluate Mediastore (Text)")
    void compareTracelinksMediastoreTextIT() {
        var similarity = SIMILARITY_MS;
        var minPrecision = MIN_PREC_MS;
        var minRecall = MIN_RECALL_MS;
        var minF1 = MIN_F1_MS;

        compareTextBased("mediastore", similarity, minPrecision, minRecall, minF1);
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

    private void compareOntologyBased(String name, double similarity, double minPrecision, double minRecall, double minF1) {
        inputText = null;
        var inputFilePath = String.format("src/test/resources/%s/%s_w_text.owl", name, name);
        inputModel = new File(inputFilePath);

        compare(name, similarity, minPrecision, minRecall, minF1);
    }

    private void compareTextBased(String name, double similarity, double minPrecision, double minRecall, double minF1) {
        var inputTextPath = String.format("src/test/resources/%s/%s.txt", name, name);
        inputText = new File(inputTextPath);
        var inputFilePath = String.format("src/test/resources/%s/%s.owl", name, name);
        inputModel = new File(inputFilePath);

        compare(name, similarity, minPrecision, minRecall, minF1);
    }

    private void compare(String name, double similarity, double minPrecision, double minRecall, double minF1) {
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, TestUtil.getSimilarityConfigString(similarity));

        var data = Pipeline.runAndSave("test_" + name, inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(data);

        var results = calculateResults(name, data);
        double precision = results.getPrecision();
        double recall = results.getRecall();
        double f1 = results.getF1();

        if (logger.isInfoEnabled()) {
            logger.info("\n{} with similarity {}:\n{}", name, similarity, results.toPrettyString());
        }

        Assertions.assertTrue(precision >= minPrecision, "Precision " + precision + " is below the expected minimum value " + minPrecision);
        Assertions.assertTrue(recall >= minRecall, "Recall " + recall + " is below the expected minimum value " + minRecall);
        Assertions.assertTrue(f1 >= minF1, "F1 " + f1 + " is below the expected minimum value " + minF1);
    }

    private EvaluationResults calculateResults(String name, AgentDatastructure data) {
        var connectionState = data.getConnectionState();
        Set<String> traceLinks = getTraceLinksFromConnectionState(connectionState);

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
}
