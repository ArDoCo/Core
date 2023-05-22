/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;

class ArDoCoForSamCodeTraceabilityLinkRecoveryTest extends CodeRunnerBaseTest {

    @Test
    @DisplayName("Test ArDoCo for SAM-Code-TLR (PCM)")
    void testSamCodeTlrPcm() {
        var runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(new File(INPUT_MODEL_ARCHITECTURE), ArchitectureModelType.PCM, new File(CodeRunnerBaseTest.inputCode), additionalConfigsMap, new File(
                OUTPUT_DIR));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    @Disabled("Disabled for faster builds. Enable if you need to check UML models.")
    @Test
    @DisplayName("Test ArDoCo for SAM-Code-TLR (UML)")
    void testSamCodeTlrUml() {
        var runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(new File(INPUT_MODEL_ARCHITECTURE_UML), ArchitectureModelType.UML, new File(CodeRunnerBaseTest.inputCode), additionalConfigsMap, new File(
                OUTPUT_DIR));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
