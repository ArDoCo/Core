package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;

public class ArDoCoForERIDTest extends RunnerBaseTest {
    private static final String DIAGRAM_DIRECTORY = "../pipeline-core/src/test/resources/";
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForERID.class);

    @Test
    @DisplayName("Test ArDoCo for ERID")
    void testLiSSA() {
        var runner = new ArDoCoForERID(PROJECT_NAME);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIGS));
        runner.setUp(DIAGRAM_DIRECTORY, INPUT_TEXT, INPUT_MODEL_ARCHITECTURE, ArchitectureModelType.PCM, additionalConfigsMap, OUTPUT_DIR);

        testRunnerAssertions(runner);
        var result = runner.run();
        Assertions.assertNotNull(result);
        var diagramRecognition = result.dataRepository().getData(DiagramRecognitionState.ID, DiagramRecognitionState.class);
        Assertions.assertTrue(diagramRecognition.isPresent());
        Assertions.assertEquals(1, diagramRecognition.get().getDiagrams().size());
    }
}
