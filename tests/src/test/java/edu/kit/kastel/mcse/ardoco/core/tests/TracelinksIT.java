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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

@RunWith(JUnitPlatform.class)
class TracelinksIT {
    private static final Logger logger = LogManager.getLogger(TracelinksIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String ADDITIONAL_CONFIG = "src/test/resources/config.properties";

    @Disabled("Disabled to not take up too much time during building. Enable and manually check to get/check results!")
    @Test
    @DisplayName("Evaluate Teastore")
    void compareTracelinksTeastoreIT() {
        var similarity = 100;
        var minPrecision = 0.62d;
        var minRecall = 0.87d;
        var minF1 = 0.73d;

        compare("teastore", similarity, minPrecision, minRecall, minF1);
    }

    @Disabled("Disabled to not take up too much time during building. Enable and manually check to get/check results!")
    @Test
    @DisplayName("Evaluate Teammates")
    void compareTracelinksTeammatesIT() {
        var similarity = 80;
        var minPrecision = 0.67d;
        var minRecall = 0.88d;
        var minF1 = 0.76d;

        compare("teammates", similarity, minPrecision, minRecall, minF1);
    }

    @Disabled("Disabled to not take up too much time during building. Enable and manually check to get/check results!")
    @Test
    @DisplayName("Evaluate Mediastore")
    void compareTracelinksMediastoreIT() {
        var similarity = 100;
        var minPrecision = 0.46d;
        var minRecall = 0.59d;
        var minF1 = 0.52d;

        compare("mediastore", similarity, minPrecision, minRecall, minF1);

    }

    private void compare(String name, int similarity, double minPrecision, double minRecall, double minF1) {
        prepareConfig(similarity);

        File inputText = null;
        var inputFilePath = String.format("src/test/resources/%s/%s_w_text.owl", name, name);
        File inputModel = new File(inputFilePath);
        File additionalConfigs = new File(ADDITIONAL_CONFIG);
        File outputDir = new File(OUTPUT);

        var data = Pipeline.run("test_" + name, inputText, inputModel, additionalConfigs, outputDir, true, false);
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

        Assertions.assertTrue(precision > minPrecision, "Precision " + precision + " is below the expected minimum value " + minPrecision);
        Assertions.assertTrue(recall > minRecall, "Recall " + recall + " is below the expected minimum value " + minRecall);
        Assertions.assertTrue(f1 > minF1, "F1 " + f1 + " is below the expected minimum value " + minF1);
    }

    private List<String> getTraceLinksFromConnectionState(IConnectionState connectionState) {
        List<String> traceLinks = Lists.mutable.empty();
        var formatString = "%s,%d";
        for (var tracelink : connectionState.getInstanceLinks()) {
            var modelUid = tracelink.getModelInstance().getUid();
            for (var nm : tracelink.getTextualInstance().getNameMappings()) {
                for (var word : nm.getWords()) {
                    var tracelinkString = String.format(formatString, modelUid, word.getSentenceNo() + 1);
                    traceLinks.add(tracelinkString);
                }
            }
        }
        return traceLinks;
    }

    private void prepareConfig(int similarity) {
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
