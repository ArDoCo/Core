/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;

class ArDoCoForSadSamCodeTraceabilityLinkRecoveryTest extends CodeRunnerBaseTest {

    @Test
    @DisplayName("Test ArDoCo for SAD-SAM-Code-TLR (PCM)")
    void testSadSamTlrPcm() {
        var runner = new ArDoCoForSadSamCodeTraceabilityLinkRecovery(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(new File(inputText), new File(inputModelArchitecture), ArchitectureModelType.PCM, new File(inputCode), additionalConfigsMap, new File(
                outputDir));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    @Test
    @DisplayName("Test ArDoCo for SAD-SAM-Code-TLR (UML)")
    void testSadSamTlrUml() {
        var runner = new ArDoCoForSadSamCodeTraceabilityLinkRecovery(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(new File(inputText), new File(inputModelArchitectureUml), ArchitectureModelType.UML, new File(inputCode), additionalConfigsMap, new File(
                outputDir));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
