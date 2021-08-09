package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        var similarity = 1.0;
        var minPrecision = 0.62d;
        var minRecall = 0.87d;
        var minF1 = 0.73d;

        compareOntologyBased("teastore", similarity, minPrecision, minRecall, minF1);
    }

    @Disabled("Disabled for CI. Enable for local test only!")
    @Test
    @DisplayName("Evaluate Teastore")
    void compareTracelinksTeastoreTextIT() {
        var similarity = 1.0;
        var minPrecision = 0.62d;
        var minRecall = 0.87d;
        var minF1 = 0.73d;

        compareTextBased("teastore", similarity, minPrecision, minRecall, minF1);
    }

    @Test
    @DisplayName("Evaluate Teammates")
    void compareTracelinksTeammatesIT() {
        var similarity = 0.80;
        var minPrecision = 0.60d;
        var minRecall = 0.82d;
        var minF1 = 0.74d;

        compareOntologyBased("teammates", similarity, minPrecision, minRecall, minF1);
    }

    // @Disabled("Disabled for CI. Enable for local test only!")
    @Test
    @DisplayName("Evaluate Teammates")
    void compareTracelinksTeammatesTextIT() {
        var similarity = 0.80;
        var minPrecision = 0.60d;
        var minRecall = 0.82d;
        var minF1 = 0.74d;

        compareTextBased("teammates", similarity, minPrecision, minRecall, minF1);
    }

    @Test
    @DisplayName("Evaluate Mediastore")
    void compareTracelinksMediastoreIT() {
        var similarity = 1.00;
        var minPrecision = 0.46d;
        var minRecall = 0.6d;
        var minF1 = 0.52d;

        compareOntologyBased("mediastore", similarity, minPrecision, minRecall, minF1);
    }

    @Disabled("Disabled for CI. Enable for local test only!")
    @Test
    @DisplayName("Evaluate Mediastore")
    void compareTracelinksMediastoreTextIT() {
        var similarity = 1.00;
        var minPrecision = 0.46d;
        var minRecall = 0.6d;
        var minF1 = 0.52d;

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
        prepareConfig(similarity);

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
        prepareConfig(similarity);

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
        List<String> traceLinks = getTraceLinksFromConnectionState(connectionState);

        var goldStandard = getGoldStandard(name);

        var results = EvalUtil.compare(traceLinks, goldStandard);
        return results;
    }

    private List<String> getTraceLinksFromConnectionState(IConnectionState connectionState) {
        var formatString = "%s,%d";
        return connectionState.getTraceLinks().collect(tl -> String.format(formatString, tl.getModelElementUid(), tl.getSentenceNumber() + 1)).castToList();
    }

    private void prepareConfig(double similarity) {
        File configFile = new File(ADDITIONAL_CONFIG);

        var settings = "similarityPercentage=" + similarity;
        try {
            FileWriter fw = new FileWriter(configFile, false);
            fw.write(settings);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
