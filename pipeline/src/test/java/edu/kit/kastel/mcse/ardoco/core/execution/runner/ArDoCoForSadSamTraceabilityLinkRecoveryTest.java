package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;

class ArDoCoForSadSamTraceabilityLinkRecoveryTest extends RunnerTestBase {

    @Test
    @DisplayName("Test ArDoCo for SAD-SAM-TLR (PCM)")
    void testSadSamTlrPcm() {
        var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(teastore);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(inputText, inputModelArchitecture, ArchitectureModelType.PCM, additionalConfigsMap, outputDir);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    @Test
    @DisplayName("Test ArDoCo for SAD-SAM-TLR (UML)")
    void testSadSamTlrUml() {
        var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(teastore);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(inputText, inputModelArchitectureUml, ArchitectureModelType.UML, additionalConfigsMap, outputDir);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
