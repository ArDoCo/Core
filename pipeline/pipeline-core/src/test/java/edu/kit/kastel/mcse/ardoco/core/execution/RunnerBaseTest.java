/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;

public class RunnerBaseTest {
    private static final Logger logger = LoggerFactory.getLogger(RunnerBaseTest.class);

    protected static final String INPUT_TEXT = "../pipeline-core/src/test/resources/teastore.txt";
    protected static final String INPUT_MODEL_ARCHITECTURE = "../pipeline-core/src/test/resources/teastore.repository";
    protected static final String INPUT_MODEL_ARCHITECTURE_UML = "../pipeline-core/src/test/resources/teastore.uml";
    protected final String OUTPUT_DIR = "../target/testout-" + this.getClass().getSimpleName();
    protected static final String ADDITIONAL_CONFIGS = "../pipeline-core/src/test/resources/additionalConfig.txt";
    protected static final String PROJECT_NAME = "teastore";

    @BeforeEach
    void setupDirectories() {
        new File(OUTPUT_DIR).mkdirs();
    }

    @AfterEach
    void cleanUp() {
        for (File file : Objects.requireNonNull(new File(OUTPUT_DIR).listFiles())) {
            if (!file.getName().equals(".gitkeep")) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    logger.warn("Error when cleaning up!", e);
                }
            }
        }
    }

    @SuppressWarnings("java:S5960")
    @Test
    @DisplayName("Test SetUp")
    void testInput() {
        File inputTextFile = new File(INPUT_TEXT);
        File inputModelArchitectureFile = new File(INPUT_MODEL_ARCHITECTURE);
        File inputModelArchitectureUmlFile = new File(INPUT_MODEL_ARCHITECTURE_UML);
        File outputDirFile = new File(OUTPUT_DIR);
        File additionalConfigsFile = new File(ADDITIONAL_CONFIGS);

        Assertions.assertAll(//
                () -> Assertions.assertTrue(inputTextFile.exists()),//
                () -> Assertions.assertTrue(inputModelArchitectureFile.exists()),//
                () -> Assertions.assertTrue(inputModelArchitectureUmlFile.exists()),//
                () -> Assertions.assertTrue(outputDirFile.exists()),//
                () -> Assertions.assertTrue(additionalConfigsFile.exists())//
        );
    }

    protected void testRunnerAssertions(ArDoCoRunner runner) {
        Assertions.assertAll(//
                () -> Assertions.assertNotNull(runner),//
                () -> Assertions.assertTrue(runner.isSetUp())//
        );
    }
}
