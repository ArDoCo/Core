/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;

class ArDoCoForInconsistencyDetectionTest extends RunnerBaseTest {

    @Test
    @DisplayName("Test ArDoCo for Inconsistency Detection (PCM)")
    void testInconsistencyDetectionPcm() {
        var runner = new ArDoCoForInconsistencyDetection(PROJECT_NAME);
        File additionalConfigsFile = new File(ADDITIONAL_CONFIGS);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(additionalConfigsFile);
        runner.setUp(INPUT_TEXT, INPUT_MODEL_ARCHITECTURE, ArchitectureModelType.PCM, additionalConfigsMap, OUTPUT_DIR);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

    @Test
    @DisplayName("Test ArDoCo for Inconsistency Detection (UML)")
    void testInconsistencyDetectionUml() {
        var runner = new ArDoCoForInconsistencyDetection(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(INPUT_TEXT, INPUT_MODEL_ARCHITECTURE_UML, ArchitectureModelType.UML, additionalConfigsMap, OUTPUT_DIR);

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }

}
