/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import static edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Classification.LABEL;

class ArDoCoForLiSSATest extends RunnerBaseTest {

    private static final String DIAGRAM_DIRECTORY = "src/test/resources/";

    @Test
    @DisplayName("Test ArDoCo for LiSSA")
    void testLiSSA() {
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
}
