/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import static edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Classification.LABEL;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;

@DisabledIfEnvironmentVariable(named = "NO_DOCKER", matches = "true")
class ArDoCoForLiSSATest extends RunnerBaseTest {

    private static final String DIAGRAM_DIRECTORY = "../pipeline-core/src/test/resources/";
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForLiSSATest.class);

    @Test
    @DisplayName("Test ArDoCo for LiSSA")
    void testLiSSA() {
        assumeDocker();
        var runner = new ArDoCoForLiSSA(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(DIAGRAM_DIRECTORY, INPUT_TEXT, INPUT_MODEL_ARCHITECTURE, ArchitectureModelType.PCM, additionalConfigsMap, OUTPUT_DIR);

        testRunnerAssertions(runner);
        var result = runner.run();
        Assertions.assertNotNull(result);
        var diagramRecognition = result.dataRepository().getData(DiagramRecognitionState.ID, DiagramRecognitionState.class);
        Assertions.assertTrue(diagramRecognition.isPresent());
        Assertions.assertEquals(1, diagramRecognition.get().getDiagrams().size());
        var diagram = diagramRecognition.get().getDiagrams().get(0);
        Assertions.assertEquals(6, diagram.getBoxes().stream().filter(it -> it.getClassification() != LABEL).count());
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
