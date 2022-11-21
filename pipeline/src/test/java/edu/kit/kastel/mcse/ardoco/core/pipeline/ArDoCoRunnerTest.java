/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ArDoCoRunnerTest {
    private final String inputText = "src/test/resources/teastore.txt";
    private final String inputModelArchitecture = "src/test/resources/teastore.repository";
    private final String outputDir = "src/test/resources/";
    private final String inputModelCode = "src/test/resources/teastore-code.json";
    private final String additionalConfigs = "src/test/resources/additionalConfig.txt";

    private static void testRunnerDefiningArdoco(ArDoCoRunner runner) {
        Assertions.assertNotNull(runner);

        ArDoCo arDoCo = ArDoCo.getInstance(runner.name());
        try {
            arDoCo.definePipeline(runner.inputText(), runner.inputModelArchitecture(), runner.inputArchitectureModelType(), runner.inputModelCode(),
                    ConfigurationHelper.loadAdditionalConfigs(runner.additionalConfigs()));
        } catch (IOException e) {
            Assertions.fail("Could not define ArDoCo");
        }
        Assertions.assertNotNull(arDoCo);
    }

    @Test
    @DisplayName("Test Builder")
    void testBuilder() {
        var builder = new ArDoCoRunner.Builder("Test").withInputText(new File(inputText))
                .withInputModelArchitecture(new File(inputModelArchitecture))
                .withPcmModelType()
                .withOutputDir(new File(outputDir))
                .withInputModelCode(new File(inputModelCode))
                .withAdditionalConfigs(new File(additionalConfigs));

        var runner = builder.build();
        testRunnerDefiningArdoco(runner);
    }

    @Test
    @DisplayName("Test Builder w/o optional arguments")
    void testBuilderWithoutOptionalArguments() {
        var builder = new ArDoCoRunner.Builder("Test").withInputText(new File(inputText))
                .withInputModelArchitecture(new File(inputModelArchitecture))
                .withPcmModelType()
                .withOutputDir(new File(outputDir));

        var runner = builder.build();
        testRunnerDefiningArdoco(runner);
    }

    @Test
    @DisplayName("Test Builder w/ incomplete arguments")
    void testBuilderIncompleteArguments() {
        var builder = new ArDoCoRunner.Builder("Test");

        Assertions.assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    @DisplayName("Test Builder w/ string paths")
    void testBuilderStringPaths() {
        var builder = new ArDoCoRunner.Builder("Test").withInputText(inputText)
                .withInputModelArchitecture(inputModelArchitecture)
                .withPcmModelType()
                .withOutputDir(outputDir)
                .withInputModelCode(inputModelCode)
                .withAdditionalConfigs(additionalConfigs);

        var runner = builder.build();
        testRunnerDefiningArdoco(runner);
    }

    @Test
    @DisplayName("Test Builder w/ additional configs provided in a map")
    void testBuilderAdditionalConfigsViaMap() {
        Map<String, String> config = Map.of("UnwantedWordsFilter::customBlacklist", "instance,item,name,product,rankings,rating,size");

        ArDoCoRunner.Builder builder = null;
        try {
            builder = new ArDoCoRunner.Builder("Test").withInputText(inputText)
                    .withInputModelArchitecture(inputModelArchitecture)
                    .withPcmModelType()
                    .withOutputDir(outputDir)
                    .withInputModelCode(inputModelCode)
                    .withAdditionalConfigs(config);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }

        var runner = builder.build();
        testRunnerDefiningArdoco(runner);
    }
}
