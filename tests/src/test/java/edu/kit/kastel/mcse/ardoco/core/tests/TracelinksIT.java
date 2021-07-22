package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

@RunWith(JUnitPlatform.class)
class TracelinksIT {

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String MODEL_W_TEXT = "src/test/resources/teastore_w_text.owl";
    private static final String NAME = "test_teastore";

    @Test
    @DisplayName("Compare found trace links to gold standard and assume a minimal F1-score")
    void compareTracelinksIT() {
        File inputText = null;
        File inputModel = new File(MODEL_W_TEXT);
        File additionalConfigs = null;
        File outputDir = new File(OUTPUT);
        boolean providedTextOntology = true;
        var data = Pipeline.run(NAME, inputText, inputModel, additionalConfigs, outputDir, providedTextOntology);

        Assertions.assertNotNull(data);
        var connectionState = data.getConnectionState();
        var tracelinks = connectionState.getInstanceLinks();
        // TODO
        var outputFormatString = "%s,%d";
        for (var tracelink : tracelinks) {
            var modelUid = tracelink.getModelInstance().getUid();
            for (var nm : tracelink.getTextualInstance().getNameMappings()) {
                for (var word : nm.getWords()) {
                    var output = String.format(outputFormatString, modelUid, word.getSentenceNo() + 1);
                    System.out.println(output);
                }
            }
        }
    }
}
