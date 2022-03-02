/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.PcmOntologyModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtractionConfig;

class InstanceRelationAgentIT {

    private static final String TEXT = "src/test/resources/benchmark/mediastore/mediastore.txt";
    private static final String MODEL = "src/test/resources/benchmark/mediastore/mediastore.owl";

    @BeforeEach
    void beforeEach() {
    }

    @AfterEach
    void afterEach() {
    }

    @Test
    @DisplayName("Test execution of InstanceRelationAgent")
    void instanceRelationIT() throws IOException {
        File inputText = ensureFile(TEXT, false);
        File inputModel = ensureFile(MODEL, false);

        var ontoConnector = new OntologyConnector(inputModel.getAbsolutePath());

        ITextConnector textConnector;
        try {
            textConnector = new ParseProvider(new FileInputStream(inputText));
        } catch (FileNotFoundException | LunaRunException | LunaInitException e) {
            Assertions.fail("Found exception when initialising ParseProvider");
            return;
        }
        IText annotatedText = textConnector.getAnnotatedText();

        IModelConnector pcmModel = new PcmOntologyModelConnector(ontoConnector);
        IExecutionStage modelExtractor = new ModelProvider(pcmModel);
        modelExtractor.exec();
        var extractorData = modelExtractor.getBlackboard();

        Assertions.assertEquals(extractorData.getModelIds().size(), 1);
        var modelId = extractorData.getModelIds().get(0);
        var data = new AgentDatastructure(annotatedText, null, extractorData.getModelState(modelId), null, null, null);

        Map<String, String> config = new HashMap<>();
        config.put("similarityPercentage", "0.75");
        config.put("Text_Agents", "InitialTextAgent PhraseAgent CorefAgent");
        IExecutionStage textModule = new TextExtraction(data, new TextExtractionConfig(config), GenericTextConfig.DEFAULT_CONFIG);
        textModule.exec();
        data.overwrite(textModule.getBlackboard());

        IExecutionStage recommendationModule = new RecommendationGenerator(data);
        recommendationModule.exec();
        data.overwrite(recommendationModule.getBlackboard());

        IExecutionStage connectionGenerator = new ConnectionGenerator(data);
        connectionGenerator.exec();
        data.overwrite(connectionGenerator.getBlackboard());

        IExecutionStage inconsistencyChecker = new InconsistencyChecker(data);
        inconsistencyChecker.exec();
        data.overwrite(inconsistencyChecker.getBlackboard());

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

        boolean hasExpected = false;
        for (IInstanceRelation relation : data.getRecommendationState(modelId).getInstanceRelations()) {
            if (relation.isIn(relator, Collections.singletonList(from), Collections.singletonList(to))) {
                hasExpected = true;
            }
        }
        assertTrue(hasExpected);
    }

    /**
     * Ensure that a file exists (or create if allowed by parameter).
     *
     * @param path   the path to the file
     * @param create indicates whether creation is allowed
     * @return the file
     * @throws IOException if something went wrong
     */
    private static File ensureFile(String path, boolean create) throws IOException {
        var file = new File(path);
        if (file.exists()) {
            return file;
        }
        if (create && file.createNewFile()) {
            return file;
        }
        // File not available
        throw new IOException("The specified file does not exist and/or could not be created: " + path);
    }
}
