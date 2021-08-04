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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

class TracelinksIT {
    private static final Logger logger = LogManager.getLogger(TracelinksIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String ADDITIONAL_CONFIG = "src/test/resources/config.properties";

    private File inputText;
    private File inputModel;
    private File additionalConfigs = new File(ADDITIONAL_CONFIG);
    private File outputDir = new File(OUTPUT);

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

    // TODO weird phenomenon: When using caching, the recall drops but the precision increases to overall better F1
    // w/o caching: 0.6827, 0.8875, 0.7717
    // w caching: 0.8250, 0.8250, 0.8250
    // Note: There is the warning that there is a change in the OntologyConnector (for OntologyWord)
    @Test
    @DisplayName("Evaluate Teammates")
    void compareTracelinksTeammatesIT() {
        var similarity = 0.80;
        var minPrecision = 0.60d;
        var minRecall = 0.82d;
        var minF1 = 0.74d;

        compareOntologyBased("teammates", similarity, minPrecision, minRecall, minF1);
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

    private void compareOntologyBased(String name, double similarity, double minPrecision, double minRecall, double minF1) {
        inputText = null;
        var inputFilePath = String.format("src/test/resources/%s/%s_w_text.owl", name, name);
        inputModel = new File(inputFilePath);

        compare(name, true, similarity, minPrecision, minRecall, minF1);
    }

    private void compareTextBased(String name, double similarity, double minPrecision, double minRecall, double minF1) {
        var inputTextPath = String.format("src/test/resources/%s/%s.txt", name, name);
        inputText = new File(inputTextPath);
        var inputFilePath = String.format("src/test/resources/%s/%s.owl", name, name);
        inputModel = new File(inputFilePath);

        compare(name, false, similarity, minPrecision, minRecall, minF1);
    }

    private void compare(String name, boolean useTextOntology, double similarity, double minPrecision, double minRecall, double minF1) {
        prepareConfig(similarity);

        var data = Pipeline.run("test_" + name, inputText, inputModel, additionalConfigs, outputDir, useTextOntology, false);
        Assertions.assertNotNull(data);

        var connectionState = data.getConnectionState();
        List<String> traceLinks = getTraceLinksFromConnectionState(connectionState);

        var goldStandard = getGoldStandard(name);

        var results = EvalUtil.compare(traceLinks, goldStandard);
        double precision = results.getPrecision();
        double recall = results.getRecall();
        double f1 = results.getF1();

        if (logger.isInfoEnabled()) {
            logger.info("\n{} with similarity {}:\n\tPrecision: {}\n\tRecall: {}\n\tF1: {}", name, similarity, precision, recall, f1);
        }

        Assertions.assertTrue(precision >= minPrecision, "Precision " + precision + " is below the expected minimum value " + minPrecision);
        Assertions.assertTrue(recall >= minRecall, "Recall " + recall + " is below the expected minimum value " + minRecall);
        Assertions.assertTrue(f1 >= minF1, "F1 " + f1 + " is below the expected minimum value " + minF1);
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
