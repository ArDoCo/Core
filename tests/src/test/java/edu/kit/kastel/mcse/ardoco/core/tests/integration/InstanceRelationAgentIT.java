/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.stage.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.PcmOntologyModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InstanceRelationAgentIT {

	private static final String TEXT = "src/test/resources/benchmark/mediastore/mediastore.txt";
	private static final String MODEL = "src/test/resources/benchmark/mediastore/mediastore.owl";

	@BeforeEach
	void beforeEach() {
	}

	@AfterEach
	void afterEach() {
	}

	@Disabled("Disabled as it is not used for now")
	@Test
	@DisplayName("Test execution of InstanceRelationAgent")
	void instanceRelationIT() throws IOException {
		var inputText = ensureFile(TEXT, false);
		var inputModel = ensureFile(MODEL, false);

		var ontoConnector = new OntologyConnector(inputModel.getAbsolutePath());

		ITextConnector textConnector;
		try {
			textConnector = new ParseProvider(new FileInputStream(inputText));
		} catch (FileNotFoundException | LunaRunException | LunaInitException e) {
			Assertions.fail("Found exception when initialising ParseProvider");
			return;
		}
		var annotatedText = textConnector.getAnnotatedText();

		IModelConnector pcmModel = new PcmOntologyModelConnector(ontoConnector);
		ModelProvider modelExtractor = new ModelProvider(pcmModel);
		var modelState = modelExtractor.execute(Map.of());
		var extractorData = new DataStructure(annotatedText, Map.of(pcmModel.getModelId(), modelState));

		Assertions.assertEquals(1, extractorData.getModelIds().size());
		var modelId = extractorData.getModelIds().get(0);
		var data = new DataStructure(annotatedText, Map.of(modelId, extractorData.getModelState(modelId)));

		Map<String, String> config = new HashMap<>();
		config.put("similarityPercentage", "0.75");
		config.put("Text_Agents", "InitialTextAgent PhraseAgent CorefAgent");
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
	 * Ensure that a file exists (or create if allowed by parameter).
	 *
	 * @param path   the path to the file
	 * @param create indicates whether creation is allowed
	 * @return the file
	 * @throws IOException if something went wrong
	 */
	private static File ensureFile(String path, boolean create) throws IOException {
		var file = new File(path);
		if (file.exists() || create && file.createNewFile()) {
			return file;
		}
		// File not available
		throw new IOException("The specified file does not exist and/or could not be created: " + path);
	}
}
