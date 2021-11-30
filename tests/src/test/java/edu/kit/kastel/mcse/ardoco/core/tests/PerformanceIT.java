/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests;


import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PerformanceIT {
    private static final Logger logger = LogManager.getLogger(PerformanceIT.class);

    private static final String OUTPUT = "src/test/resources/testout";

    private File inputText;
    private File inputModel;
    private File additionalConfigs = null;
    private File outputDir = new File(OUTPUT);
    private String name = "teammates";

    @Disabled("Only for individual testing, not for CI")
    @Test
    @DisplayName("Time Teammates Ontology")
    void timeTeammatesOntologyIT() {
        name = "teammates";
        prepareOntology();

        var duration = measureExecution();

        Assertions.assertTrue(duration.toSeconds() < 420);
    }

    @Disabled("Only for individual testing, not for CI")
    @Test
    @DisplayName("Time Teammates")
    void timeTeammatesTextIT() {
        name = "teammates";
        prepareText();

        var duration = measureExecution();

        Assertions.assertTrue(duration.toSeconds() < 110);
    }

    @Disabled("Only for individual testing, not for CI")
    @Test
    @DisplayName("Time Teastore Ontology")
    void timeTeastoreOntologyIT() {
        name = "teastore";
        prepareOntology();

        var duration = measureExecution();

        Assertions.assertTrue(duration.toSeconds() < 60);
    }

    private Duration measureExecution() {
        Instant start = Instant.now();
        Pipeline.runAndSave("test_time_" + name, inputText, inputModel, additionalConfigs, outputDir);
        Instant end = Instant.now();

        var duration = Duration.between(start, end);
        logger.info("Finished in {}.{}s.", duration.getSeconds(), duration.toMillisPart());
        return duration;
    }

    private void prepareOntology() {
        inputText = null;
        var inputFilePath = String.format("src/test/resources/%s/%s_w_text.owl", name, name);
        inputModel = new File(inputFilePath);
    }

    private void prepareText() {
        var inputTextPath = String.format("src/test/resources/%s/%s.txt", name, name);
        inputText = new File(inputTextPath);
        var inputFilePath = String.format("src/test/resources/%s/%s.owl", name, name);
        inputModel = new File(inputFilePath);
    }

}
