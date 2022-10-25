/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArDoCoCLITest {
    private static final String OUTPUT = "src/test/resources/testout";
    private static final String TEXT = "../tests/src/test/resources/benchmark/teastore/text_2020/teastore.txt";
    private static final String MODEL = "../tests/src/test/resources/benchmark/teastore/model_2020/pcm/teastore.repository";
    private static final String MODEL_UML = "../tests/src/test/resources/benchmark/teastore/model_2020/uml/teastore.uml";
    private static final String NAME = "teastore";

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("log4j.configurationFile", "src/main/resources/log4j2.xml");
    }

    @Test
    @DisplayName("Testing CLI with provided text file")
    void pipelineWithTextTest() {
        String[] args = { "-n", NAME, "-ma", MODEL, "-t", TEXT, "-o", OUTPUT };
        var runner = ArDoCoCLI.parseCommandLineAndBuildArDoCoRunner(args);

        Assertions.assertNotNull(runner);

        var additionalConfigs = ArDoCo.loadAdditionalConfigs(runner.additionalConfigs());
        ArDoCo arDoCo = ArDoCo.getInstance(runner.name());
        try {
            arDoCo.definePipeline(runner.inputText(), runner.inputModelArchitecture(), runner.inputArchitectureModelType(), runner.inputModelCode(),
                    additionalConfigs);
        } catch (IOException e) {
            Assertions.fail("Could not define ArDoCo");
        }
        Assertions.assertNotNull(arDoCo);
    }

    @Test
    @DisplayName("Testing CLI with provided text file and UML Model")
    void pipelineWithTextAndUMLTest() {
        String[] args = { "-n", NAME, "-ma", MODEL_UML, "-mt", ArchitectureModelType.UML.name(), "-t", TEXT, "-o", OUTPUT };
        var runner = ArDoCoCLI.parseCommandLineAndBuildArDoCoRunner(args);

        Assertions.assertNotNull(runner);

        var additionalConfigs = ArDoCo.loadAdditionalConfigs(runner.additionalConfigs());
        ArDoCo arDoCo = ArDoCo.getInstance(runner.name());
        try {
            arDoCo.definePipeline(runner.inputText(), runner.inputModelArchitecture(), runner.inputArchitectureModelType(), runner.inputModelCode(),
                    additionalConfigs);
        } catch (IOException e) {
            Assertions.fail("Could not define ArDoCo");
        }
        Assertions.assertNotNull(arDoCo);
    }

    @Test
    @DisplayName("Testing CLI without provided text file")
    void pipelineWithProvidedWrongTextTest() {
        String[] args = { "-n", NAME, "-ma", MODEL, "-o", OUTPUT };
        var runner = ArDoCoCLI.parseCommandLineAndBuildArDoCoRunner(args);
        Assertions.assertNull(runner);
    }

    @Test
    @DisplayName("Testing CLI with wrong text")
    void pipelineWithNonexistentTextTest() {
        String[] args = { "-n", NAME, "-ma", MODEL, "-t", "NONEXISTENT", "-o", OUTPUT };
        var runner = ArDoCoCLI.parseCommandLineAndBuildArDoCoRunner(args);
        Assertions.assertNull(runner);
    }

    @Test
    @DisplayName("Testing CLI with wrong model")
    void pipelineWithNonexistentModelTest() {
        String[] args = { "-n", NAME, "-ma", "NONEXISTENT", "-t", TEXT, "-o", OUTPUT };
        var runner = ArDoCoCLI.parseCommandLineAndBuildArDoCoRunner(args);
        Assertions.assertNull(runner);
    }
}
