package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;

import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.exception.InconsistentModelException;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.PcmOntologyModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.pipeline.helpers.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.textextractor.TextExtractor;

public class Pipeline {

	public static void main(String[] args) throws LunaRunException {
		InputStream documentation = Pipeline.class.getResourceAsStream(PipelineConfig.DOCUMENTATION_PATH);
		// InputStream test =
		// Pipeline.class.getResourceAsStream(PipelineConfig.TEST_DOCUMENTATION_PATH);
		run(documentation);
	}

	public static void run(InputStream text) throws LunaRunException {

		new File("evaluations").mkdirs();
		new File("ecsaEvaluations").mkdirs();

		long startTime = System.currentTimeMillis();

		ITextConnector connector = new ParseProvider(text);
		IText annotatedText = connector.getAnnotatedText();

		IModelConnector pcmTeammatesModel = new PcmOntologyModelConnector("src/main/resources/teammates.owl");
		FilePrinter.writeModelInstancesInCsvFile(null, runModelExtractor(pcmTeammatesModel), "Teammates");

		// IModelConnector mediaStoreModel = new
		// PcmOntologyModelConnector("src/main/resources/mediastore.owl");
		// FilePrinter.writeModelInstancesInCsvFile(null,
		// runModelExtractor(mediaStoreModel), "MediaStore");

		// IModelConnector teaStoreModel = new
		// PcmOntologyModelConnector("src/main/resources/TeaStore.owl");
		// FilePrinter.writeModelInstancesInCsvFile(null,
		// runModelExtractor(teaStoreModel), "TeaStore");

		AgentDatastructure data = new AgentDatastructure(annotatedText, null, runModelExtractor(pcmTeammatesModel), null, null);
		data.overwrite(runTextExtractor(data));
		data.overwrite(runRecommendationGenerator(data));
		data.overwrite(runConnectionGenerator(data));

		Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);

		printResultsInFiles(data, duration);
	}

	private static void printResultsInFiles(//
			AgentDatastructure data, Duration duration) {

		FilePrinter.writeEval1ToFile(data.getText(), data.getTextState(), 0);
		// FilePrinter.writeRecommendationsToFile(recommendationState, 0);
		// FilePrinter.writeRecommendedRelationToFile(recommendationState);
		// FilePrinter.writeConnectionsToFile(connectionState, 0);
		// FilePrinter.writeConnectionRelationsToFile(connectionState);

		FilePrinter.writeStatesToFile(data.getModelState(), data.getTextState(), data.getRecommendationState(), data.getConnectionState(), duration);
		FilePrinter.writeNounMappingsInCsvFile(null, data.getTextState());
		FilePrinter.writeTraceLinksInCsvFile(null, data.getConnectionState());

	}

	private static IModelState runModelExtractor(IModelConnector modelConnector) throws InconsistentModelException {
		IModule<IModelState> hardCodedModelExtractor = new ModelProvider(modelConnector);
		hardCodedModelExtractor.exec();
		return hardCodedModelExtractor.getState();
	}

	private static AgentDatastructure runTextExtractor(AgentDatastructure data) {
		IModule<AgentDatastructure> textModule = new TextExtractor(data);
		textModule.exec();
		return textModule.getState();
	}

	private static AgentDatastructure runRecommendationGenerator(AgentDatastructure data) {
		IModule<AgentDatastructure> recommendationModule = new RecommendationGenerator(data);
		recommendationModule.exec();
		return recommendationModule.getState();
	}

	private static AgentDatastructure runConnectionGenerator(AgentDatastructure data) {
		IModule<AgentDatastructure> mcAgent = new ConnectionGenerator(data);
		mcAgent.exec();
		return mcAgent.getState();
	}

}
