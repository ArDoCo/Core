package edu.kit.ipd.consistency_analyzer.pipeline;

import java.io.File;
import java.io.InputStream;

import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.modelproviders.HardCodedModelConnector;
import edu.kit.ipd.consistency_analyzer.modelproviders.HardCodedModelInput;
import edu.kit.ipd.consistency_analyzer.modelproviders.IModelConnector;
import edu.kit.ipd.consistency_analyzer.modelproviders.exception.InconsistentModelException;
import edu.kit.ipd.consistency_analyzer.modules.ConnectionGenerator;
import edu.kit.ipd.consistency_analyzer.modules.IModule;
import edu.kit.ipd.consistency_analyzer.modules.ModelExtractor;
import edu.kit.ipd.consistency_analyzer.modules.RecommendationGenerator;
import edu.kit.ipd.consistency_analyzer.modules.TextExtractor;
import edu.kit.ipd.consistency_analyzer.pipeline.helpers.FilePrinter;
import edu.kit.ipd.consistency_analyzer.textproviders.ITextConnector;
import edu.kit.ipd.consistency_analyzer.textproviders.ParseProvider;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;

public class Pipeline {

	static {
		new File("evaluations").mkdirs();
	}
	static InputStream documentation = Pipeline.class.getResourceAsStream(PipelineConfig.DOCUMENTATION_PATH);
	static InputStream test = Pipeline.class.getResourceAsStream(PipelineConfig.TEST_DOCUMENTATION_PATH);

	public static void main(String args[]) throws NoSuchMethodException, InconsistentModelException, PipelineStageException, MissingDataException {
		run(documentation);
	}

	public static void run(InputStream text) throws NoSuchMethodException, InconsistentModelException, PipelineStageException, MissingDataException {
		long startTime = System.currentTimeMillis();

		ITextConnector connector = new ParseProvider(text);
		IText annotatedText = connector.getAnnotatedText();

		HardCodedModelInput hardCodedModel = new HardCodedModelInput();
		IModelConnector modelConnector = new HardCodedModelConnector(hardCodedModel);

		IModelExtractionState modelExtractionState = runModelExtractor(modelConnector);
		ITextExtractionState textExtractionState = runTextExtractor(annotatedText);
		IRecommendationState recommendationState = runRecommendationGenerator(annotatedText, modelExtractionState, textExtractionState);
		IConnectionState connectionState = runConnectionGenerator(annotatedText, modelExtractionState, textExtractionState, recommendationState);

		double duration = (System.currentTimeMillis() - startTime) / 1000.0;
		double min = duration / 60;

		printResultsInFiles(annotatedText, modelExtractionState, textExtractionState, recommendationState, connectionState, min);
	}

	private static void printResultsInFiles(//
			IText text, IModelExtractionState modelExtractionState, ITextExtractionState textExtractionState, //
			IRecommendationState recommendationState, IConnectionState connectionState, double min) {

		// FilePrinter.writeEval1ToFile(text, textExtractionState, 0);
		// FilePrinter.writeRecommendationsToFile(recommendationState, 0);
		// FilePrinter.writeRecommendedRelationToFile(recommendationState);
		// FilePrinter.writeConnectionsToFile(connectionState, 0);
		// FilePrinter.writeConnectionRelationsToFile(connectionState);
		FilePrinter.writeStatesToFile(modelExtractionState, textExtractionState, recommendationState, connectionState, min);

	}

	private static IModelExtractionState runModelExtractor(IModelConnector modelConnector) throws InconsistentModelException {
		IModule<IModelExtractionState> hardCodedModelExtractor = new ModelExtractor(modelConnector);
		hardCodedModelExtractor.exec();
		return hardCodedModelExtractor.getState();
	}

	private static ITextExtractionState runTextExtractor(IText graph) {
		IModule<ITextExtractionState> textModule = new TextExtractor(graph);
		textModule.exec();
		return textModule.getState();
	}

	private static IRecommendationState runRecommendationGenerator(IText graph, IModelExtractionState modelExtractionState, ITextExtractionState textExtractionState) {
		IModule<IRecommendationState> recommendationModule = new RecommendationGenerator(graph, modelExtractionState, textExtractionState);
		recommendationModule.exec();
		return recommendationModule.getState();
	}

	private static IConnectionState runConnectionGenerator(IText graph, IModelExtractionState modelExtractionState, ITextExtractionState textExtractionState,
			IRecommendationState recommendationState) {
		IModule<IConnectionState> mcAgent = new ConnectionGenerator(graph, modelExtractionState, textExtractionState, recommendationState);
		mcAgent.exec();
		return mcAgent.getState();
	}

}
