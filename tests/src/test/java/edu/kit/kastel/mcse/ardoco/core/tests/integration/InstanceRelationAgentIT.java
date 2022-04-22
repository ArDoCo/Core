/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.stage.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

@Disabled("Disabled as it is not used for now")
class InstanceRelationAgentIT {

    private static final String TEXT = "src/test/resources/benchmark/mediastore/mediastore.txt";
    private static final String MODEL = "src/test/resources/benchmark/mediastore/original_model/ms.repository";

    @Test
    @DisplayName("Test execution of InstanceRelationAgent")
    void instanceRelationIT() throws IOException, ReflectiveOperationException {
        var inputText = ensureFile(TEXT);
        var inputModel = ensureFile(MODEL);

        ITextConnector textConnector;
        try {
            textConnector = new ParseProvider(new FileInputStream(inputText));
        } catch (FileNotFoundException | LunaRunException | LunaInitException e) {
            Assertions.fail("Found exception when initialising ParseProvider");
            return;
        }
        var annotatedText = textConnector.getAnnotatedText();

        IModelConnector pcmModel = new PcmXMLModelConnector(new File(inputModel.getAbsolutePath()));
        var modelExtractor = new ModelProvider(pcmModel);
        var modelState = modelExtractor.execute(Map.of());
        var extractorData = new DataStructure(annotatedText, Map.of(pcmModel.getModelId(), modelState));

        Assertions.assertEquals(1, extractorData.getModelIds().size());
        var modelId = extractorData.getModelIds().get(0);
        var data = new DataStructure(annotatedText, Map.of(modelId, extractorData.getModelState(modelId)));

        Map<String, String> config = new HashMap<>();
        config.put(TextExtraction.class.getSimpleName() + "::" + "enabledAgents", "InitialTextAgent,PhraseAgent,CorefAgent");

        IExecutionStage textModule = new TextExtraction();
        textModule.execute(data, config);

        IExecutionStage recommendationModule = new RecommendationGenerator();
        recommendationModule.execute(data, Map.of());

        IExecutionStage connectionGenerator = new ConnectionGenerator();
        connectionGenerator.execute(data, Map.of());

        IExecutionStage inconsistencyChecker = new InconsistencyChecker();
        inconsistencyChecker.execute(data, Map.of());

        IWord relator = null;
        IWord from = null;
        IWord to = null;
        for (IWord word : data.getText().getWords()) {
            if (word.getPosition() == 481) {
                relator = word;
            } else if (word.getPosition() == 480) {
                from = word;
            } else if (word.getPosition() == 483) {
                to = word;
            }
        }

        var hasExpected = false;
        for (IInstanceRelation relation : data.getRecommendationState(pcmModel.getMetamodel()).getInstanceRelations()) {
            if (relation.isIn(relator, Collections.singletonList(from), Collections.singletonList(to))) {
                hasExpected = true;
            }
        }
        assertTrue(hasExpected);
    }

    /**
     * Ensure that a file exists.
     *
     * @param path the path to the file
     * @return the file
     * @throws IOException if something went wrong
     */
    private static File ensureFile(String path) throws IOException {
        var file = new File(path);
        if (file.exists()) {
            return file;
        }
        // File not available
        throw new IOException("The specified file does not exist and/or could not be created: " + path);
    }
}
