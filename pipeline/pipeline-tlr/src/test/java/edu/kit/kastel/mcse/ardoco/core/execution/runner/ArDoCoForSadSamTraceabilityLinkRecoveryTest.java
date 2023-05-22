/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCoForSadSamTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;

@Disabled("Disabled as other (integration) tests cover the same functionality. Enable for debugging/local development.")
class ArDoCoForSadSamTraceabilityLinkRecoveryTest extends RunnerBaseTest {

    @Test
    @DisplayName("Test ArDoCo for SAD-SAM-TLR (PCM)")
    void testSadSamTlrPcm() {
        var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(INPUT_TEXT, INPUT_MODEL_ARCHITECTURE, ArchitectureModelType.PCM, additionalConfigsMap, OUTPUT_DIR);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    @Disabled("Disabled for faster builds. Enable if you need to check UML models.")
    @Test
    @DisplayName("Test ArDoCo for SAD-SAM-TLR (UML)")
    void testSadSamTlrUml() {
        var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(INPUT_TEXT, INPUT_MODEL_ARCHITECTURE_UML, ArchitectureModelType.UML, additionalConfigsMap, OUTPUT_DIR);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
