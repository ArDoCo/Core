package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RunnerTestBase {
    protected final String inputText = "src/test/resources/teastore.txt";
    protected final String inputModelArchitecture = "src/test/resources/teastore.repository";
    protected final String inputModelArchitectureUml = "src/test/resources/teastore.uml";
    protected final String outputDir = "src/test/resources/testout";
    protected final String inputModelCode = "src/test/resources/teastore-code.json";
    protected final String additionalConfigs = "src/test/resources/additionalConfig.txt";
    protected final String teastore = "teastore";

    @Test
    @DisplayName("Test SetUp")
    void testInput() {
        File inputTextFile = new File(inputText);
        File inputModelArchitectureFile = new File(inputModelArchitecture);
        File inputModelArchitectureUmlFile = new File(inputModelArchitectureUml);
        File outputDirFile = new File(outputDir);
        File inputModelCodeFile = new File(inputModelCode);
        File additionalConfigsFile = new File(additionalConfigs);

        Assertions.assertAll(//
                () -> Assertions.assertTrue(inputTextFile.exists()),//
                () -> Assertions.assertTrue(inputModelArchitectureFile.exists()),//
                () -> Assertions.assertTrue(inputModelArchitectureUmlFile.exists()),//
                () -> Assertions.assertTrue(outputDirFile.exists()),//
                () -> Assertions.assertTrue(inputModelCodeFile.exists()),//
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
