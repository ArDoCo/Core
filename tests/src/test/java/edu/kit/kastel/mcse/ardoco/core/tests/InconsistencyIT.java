package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.IEvaluationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model.DeleteOneModelElementEval;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
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
    @DisplayName("Evaluate Inconsistency Analyses for Teammates")
    void inconsistencyTeammatesIT() {
        DeleteOneModelElementEval eval1 = new DeleteOneModelElementEval();

        var outFile = OUTPUT + File.separator + "inconsistency-eval-teammates.txt";

        try (PrintStream os = new PrintStream(new File(outFile))) {
            var results = run(Projects.TEAMMATES, eval1, os);
            Assertions.assertNotNull(results);
        } catch (FileNotFoundException e) {
            Assertions.assertTrue(false, "Could not find file.");
        }
    }

    @Test
    @DisplayName("Evaluate Inconsistency Analyses for Mediastore")
    void inconsistencyMediastoreIT() {
        DeleteOneModelElementEval eval1 = new DeleteOneModelElementEval();

        var outFile = OUTPUT + File.separator + "inconsistency-eval-mediastore.txt";

        try (PrintStream os = new PrintStream(new File(outFile))) {
            var results = run(Projects.MEDIASTORE, eval1, os);
            Assertions.assertNotNull(results);
        } catch (FileNotFoundException e) {
            Assertions.assertTrue(false, "Could not find file.");
        }
    }

    @Test
    @DisplayName("Evaluate Inconsistency Analyses for Teastore")
    void inconsistencyTeastoreIT() {
        DeleteOneModelElementEval eval1 = new DeleteOneModelElementEval();

        var outFile = OUTPUT + File.separator + "inconsistency-eval-teastore.txt";

        try (PrintStream os = new PrintStream(new File(outFile))) {
            var results = run(Projects.TEASTORE, eval1, os);
            Assertions.assertNotNull(results);
        } catch (FileNotFoundException e) {
            Assertions.assertTrue(false, "Could not find file.");
        }
    }

    private static EvaluationResult run(Projects project, IEvaluationStrategy eval, PrintStream os) {
        os.println("####################################");
        os.println("START Eval: " + project + " -- " + eval);

        IModelConnector pcmModel = project.getModel();
        IText annotatedText = project.getText();

        GoldStandard gs = project.getGoldStandard(pcmModel);
        var results = eval.evaluate(project, pcmModel, annotatedText, gs, os);

        os.println("END Eval: " + project + " -- " + eval);
        os.println("####################################\n");
        return results;
    }

    // OLD Tests

    @Disabled("Outdated")
    @Test
    @DisplayName("test inconsistency detection with original input")
    void inconsistencyIT() {
        var configOptions = new String[] { TestUtil.getSimilarityConfigString(0.8), TestUtil.getMmeiThresholdConfigString(0.75) };
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, configOptions);

        inputText = null;
        inputModel = new File("src/test/resources/teammates/inconsistency/tm_w_text.owl");

        logger.info("Running Inconsistency IT for Teammates");
        var data = Pipeline.runAndSave("test_teammates_inconsistency", inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(data);
    }

    @Disabled("Outdated")
    @Test
    @DisplayName("test inconsistency detection when one element got deleted from model")
    void inconsistencyWithDeletedModelElementIT() {
        var configOptions = new String[] { TestUtil.getSimilarityConfigString(0.8), TestUtil.getMmeiThresholdConfigString(0.75) };
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, configOptions);

        inputText = null;
        inputModel = new File("src/test/resources/teammates/inconsistency/tm_w_text_wo_ui.owl");

        logger.info("Running Inconsistency IT for Teammates missing UI Model Element");
        var data = Pipeline.runAndSave("test_teammates_inconsistency_wo_ui", inputText, inputModel, additionalConfigs, outputDir);

        Assertions.assertNotNull(data);
    }

    @Disabled("Disabled for CI")
    @Test
    @DisplayName("test inconsistency detection with original input and provided text")
    void inconsistencyUsingTextIT() {
        var configOptions = new String[] { TestUtil.getSimilarityConfigString(0.8), TestUtil.getMmeiThresholdConfigString(0.75) };
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, configOptions);

        inputText = new File("src/test/resources/teammates/teammates.txt");
        inputModel = new File("src/test/resources/teammates/inconsistency/tm.owl");

        logger.info("Running Inconsistency IT for Teammates with Text");
        var data = Pipeline.runAndSave("test_teammates_inconsistency_text", inputText, inputModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(data);
    }

    @Disabled("Disabled for CI")
    @Test
    @DisplayName("test inconsistency detection with provided text and one element got deleted from model")
    void inconsistencyWithDeletedModelElementUsingTextIT() {
        var configOptions = new String[] { TestUtil.getSimilarityConfigString(0.8), TestUtil.getMmeiThresholdConfigString(0.75) };
        TestUtil.setConfigOptions(ADDITIONAL_CONFIG, configOptions);

        inputText = new File("src/test/resources/teammates/teammates.txt");
        inputModel = new File("src/test/resources/teammates/inconsistency/tm_wo_ui.owl");

        logger.info("Running Inconsistency IT for Teammates with Text and missing model element");
        var data = Pipeline.runAndSave("test_teammates_inconsistency_wo_ui_text", inputText, inputModel, additionalConfigs, outputDir);

        Assertions.assertNotNull(data);
    }

}
