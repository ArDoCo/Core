/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultArDoCoRunnerTest extends RunnerTestBase {

    @Test
    @DisplayName("Test Builder")
    void testBuilder() {
        var builder = new DefaultArDoCoRunner.Builder("Test");
        builder.withInputText(new File(inputText))
                .withInputModelArchitecture(new File(inputModelArchitecture))
                .withPcmModelType()
                .withOutputDir(new File(outputDir))
                .withInputModelCode(new File(inputModelCode))
                .withAdditionalConfigs(new File(additionalConfigs));

        var runner = builder.build();
        testRunnerAssertions(runner);
    }

    @Test
    @DisplayName("Test Builder")
    void testBuilderUml() {
        var builder = new DefaultArDoCoRunner.Builder("Test");
        builder.withInputText(new File(inputText))
                .withInputModelArchitecture(new File(inputModelArchitectureUml))
                .withUmlModelType()
                .withOutputDir(new File(outputDir))
                .withInputModelCode(new File(inputModelCode))
                .withAdditionalConfigs(new File(additionalConfigs));

        var runner = builder.build();
        testRunnerAssertions(runner);
    }

    @Test
    @DisplayName("Test Builder w/o optional arguments")
    void testBuilderWithoutOptionalArguments() {
        var builder = new DefaultArDoCoRunner.Builder("Test");
        builder.withInputText(new File(inputText))
                .withInputModelArchitecture(new File(inputModelArchitecture))
                .withPcmModelType()
                .withOutputDir(new File(outputDir));

        var runner = builder.build();
        testRunnerAssertions(runner);
    }

    @Test
    @DisplayName("Test Builder w/ incomplete arguments")
    void testBuilderIncompleteArguments() {
        var builder = new DefaultArDoCoRunner.Builder("Test");

        Assertions.assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    @DisplayName("Test Builder w/ string paths")
    void testBuilderStringPaths() {
        var builder = new DefaultArDoCoRunner.Builder("Test");
        builder.withInputText(inputText)
                .withInputModelArchitecture(inputModelArchitecture)
                .withPcmModelType()
                .withOutputDir(outputDir)
                .withInputModelCode(inputModelCode)
                .withAdditionalConfigs(additionalConfigs);

        var runner = builder.build();
        testRunnerAssertions(runner);
    }

    @Test
    @DisplayName("Test Builder w/ additional configs provided in a map")
    void testBuilderAdditionalConfigsViaMap() {
        Map<String, String> config = Map.of("UnwantedWordsFilter::customBlacklist", "instance,item,name,product,rankings,rating,size",
                "UnwantedWordsFilter::enableCommonBlacklist", "true");

        DefaultArDoCoRunner.Builder builder = null;
        try {
            builder = new DefaultArDoCoRunner.Builder("Test");
            builder.withInputText(inputText)
                    .withInputModelArchitecture(inputModelArchitecture)
                    .withPcmModelType()
                    .withOutputDir(outputDir)
                    .withInputModelCode(inputModelCode)
                    .withAdditionalConfigs(config);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }

        var runner = builder.build();
        testRunnerAssertions(runner);
    }
}
