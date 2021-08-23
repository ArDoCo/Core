package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;

class InconsistencyIT {
    private static final Logger logger = LogManager.getLogger(InconsistencyIT.class);

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
    @DisplayName("test inconsistency detection with original input")
    void inconsistencyIT() {
        var configOptions = new String[] { TestUtil.getSimilarityConfigString(0.8), TestUtil.getMmeiThresholdConfigString(0.75) };
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, configOptions);

        inputText = null;
        inputModel = new File("src/test/resources/teammates/inconsistency/tm.owl");

        logger.info("Running Inconsistency IT for Teammates");
        var data = Pipeline.runAndSave("test_teammates_inconsistency", inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(data);
    }

    @Test
    @DisplayName("test inconsistency detection when one element got deleted from model")
    void inconsistencyWithDeletedModelElementIT() {
        var configOptions = new String[] { TestUtil.getSimilarityConfigString(0.8), TestUtil.getMmeiThresholdConfigString(0.75) };
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, configOptions);

        inputText = null;
        inputModel = new File("src/test/resources/teammates/inconsistency/tm_wo_ui.owl");

        logger.info("Running Inconsistency IT for Teammates missing UI Model Element");
        var data = Pipeline.runAndSave("test_teammates_inconsistency_wo_ui", inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(data);
    }

}
