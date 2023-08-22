/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;

@DisabledIfEnvironmentVariable(named = "NO_DOCKER", matches = "true")
class ArDoCoSadDiagramCodeTraceabilityLinkRecoveryTest extends RunnerBaseTest {

    private static final String DIAGRAM_DIRECTORY = "../pipeline-core/src/test/resources/";
    private static final String CODE_DIRECTORY = CodeProject.TEASTORE.getCodeModelDirectory();
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForLiSSATest.class);

    @Test
    @DisplayName("Test ArDoCo for SadDiagramCodeTraceabilityLinkRecovery")
    void testArDoCoSadDiagramCodeTraceabilityLinkRecovery() {
        assumeDocker();
        var runner = new ArDoCoSadDiagramCodeTraceabilityLinkRecovery(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(INPUT_TEXT, DIAGRAM_DIRECTORY, CODE_DIRECTORY, additionalConfigsMap, OUTPUT_DIR);

        testRunnerAssertions(runner);

        var result = runner.run();
        Assertions.assertNotNull(result);
    }

    private void assumeDocker() {
        boolean remoteDocker = System.getenv("REMOTE_DOCKER_IP") != null && System.getenv("REMOTE_DOCKER_PORT") != null;
        boolean localDocker;
        try {
            var result = Runtime.getRuntime().exec("docker ps");
            result.waitFor(3, TimeUnit.SECONDS);
            localDocker = result.exitValue() == 0;
        } catch (Exception e) {
            localDocker = false;
            logger.error(e.getMessage(), e);
        }
        Assumptions.assumeTrue(remoteDocker || localDocker, "Docker is not available ..");
    }
}
