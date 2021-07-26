package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

@RunWith(JUnitPlatform.class)
class TracelinksIT {
    private static final Logger logger = LogManager.getLogger();

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String MODEL_W_TEXT = "src/test/resources/teastore_w_text.owl";
    private static final String NAME = "test_teastore";
    private static final String ADDITIONAL_CONFIG = "src/test/resources/config.properties";
    private static final int SIMILARITY_TS = 100;

    @Test
    @DisplayName("Compare found trace links to gold standard and assume minimal Precision, Recall, and F1-score")
    void compareTracelinksIT() {
        prepareConfig(SIMILARITY_TS);

        File inputText = null;
        File inputModel = new File(MODEL_W_TEXT);
        File additionalConfigs = new File(ADDITIONAL_CONFIG);
        File outputDir = new File(OUTPUT);
        boolean providedTextOntology = true;
        var data = Pipeline.run(NAME, inputText, inputModel, additionalConfigs, outputDir, providedTextOntology);

        Assertions.assertNotNull(data);

        var connectionState = data.getConnectionState();
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

        var goldStandard = getGoldStandardTeastore();

        var results = EvalUtil.compare(traceLinks, goldStandard);
        Assertions.assertTrue(results.getPrecision() > 0.62d, "Precision is below the expected minimum value.");
        Assertions.assertTrue(results.getRecall() > 0.87d, "Recall is below the expected minimum value.");
        Assertions.assertTrue(results.getF1() > 0.73d, "F1 is below the expected minimum value.");

        if (logger.isInfoEnabled()) {
            logger.info("\n{} with similarity {}:\n\tPrecision: {}\n\tRecall: {}\n\tF1: {}", NAME, SIMILARITY_TS, results.getPrecision(), results.getRecall(),
                    results.getF1());
        }

    }

    private void prepareConfig(int similarity) {
        File configFile = new File(ADDITIONAL_CONFIG);

        var settings = "similarityPercentage=" + similarity;
        try {
            FileWriter f2 = new FileWriter(configFile, false);
            f2.write(settings);
            f2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getGoldStandardTeastore() {
        List<String> goldLinks = Lists.mutable.empty();
        goldLinks.add("_a3CcMJUXEeqcD4b2aEjF3w,1");
        goldLinks.add("_lA4vAJuDEeqFLuz8hc-8Wg,2");
        goldLinks.add("_CkKuMJUXEeqcD4b2aEjF3w,2");
        goldLinks.add("_3fZZMJUWEeqcD4b2aEjF3w,3");
        goldLinks.add("_Kjt-sJUXEeqcD4b2aEjF3w,4");
        goldLinks.add("_ZfYFsJUXEeqcD4b2aEjF3w,4");
        goldLinks.add("_CkKuMJUXEeqcD4b2aEjF3w,5");
        goldLinks.add("_lA4vAJuDEeqFLuz8hc-8Wg,7");
        goldLinks.add("_CkKuMJUXEeqcD4b2aEjF3w,7");
        goldLinks.add("_CkKuMJUXEeqcD4b2aEjF3w,8");
        goldLinks.add("_lA4vAJuDEeqFLuz8hc-8Wg,10");
        goldLinks.add("_CkKuMJUXEeqcD4b2aEjF3w,10");
        goldLinks.add("_lA4vAJuDEeqFLuz8hc-8Wg,12");
        goldLinks.add("_3fZZMJUWEeqcD4b2aEjF3w,18");
        goldLinks.add("_ZfYFsJUXEeqcD4b2aEjF3w,22");
        goldLinks.add("_K8BTQJXCEeqTkvVy2rVPEA,23");
        goldLinks.add("_K8BTQJXCEeqTkvVy2rVPEA,24");
        goldLinks.add("_ZfYFsJUXEeqcD4b2aEjF3w,25");
        goldLinks.add("_ZfYFsJUXEeqcD4b2aEjF3w,26");
        goldLinks.add("_Kjt-sJUXEeqcD4b2aEjF3w,27");
        goldLinks.add("_0ntBwMTWEeqPMY5aZpc07Q,32");
        goldLinks.add("_a3CcMJUXEeqcD4b2aEjF3w,37");
        goldLinks.add("_a3CcMJUXEeqcD4b2aEjF3w,38");
        goldLinks.add("_a3CcMJUXEeqcD4b2aEjF3w,41");
        goldLinks.add("_a3CcMJUXEeqcD4b2aEjF3w,43");
        return goldLinks;
    }
}
