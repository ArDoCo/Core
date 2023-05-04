/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RunnerBaseTest {
    protected final String inputText = "src/test/resources/teastore.txt";
    protected final String inputModelArchitecture = "src/test/resources/teastore.repository";
    protected final String inputModelArchitectureUml = "src/test/resources/teastore.uml";
    protected final String outputDir = "src/test/resources/testout";
    protected final String additionalConfigs = "src/test/resources/additionalConfig.txt";
    protected final String projectName = "teastore";

    @AfterEach
    void cleanUp() {
        for (File file : new File(outputDir).listFiles()) {
            if (!file.getName().equals(".gitkeep")) {
                file.delete();
            }
        }
    }

    @Test
    @DisplayName("Test SetUp")
    void testInput() {
        File inputTextFile = new File(inputText);
        File inputModelArchitectureFile = new File(inputModelArchitecture);
        File inputModelArchitectureUmlFile = new File(inputModelArchitectureUml);
        File outputDirFile = new File(outputDir);
        File additionalConfigsFile = new File(additionalConfigs);

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
