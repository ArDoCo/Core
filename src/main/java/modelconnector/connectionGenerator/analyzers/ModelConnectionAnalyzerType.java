package modelconnector.connectionGenerator.analyzers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Factory class for the creation of model connection analyzers.
 *
 * @author Sophie
 *
 */
public enum ModelConnectionAnalyzerType {

	/**
	 * This is an empty spot for some model connection analyzers as enums.
	 */
	;

	private Crea c;

	ModelConnectionAnalyzerType(Crea c) {
		this.c = c;
	}

	/**
	 * Creates a new analyzer.
	 *
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to look up
	 * @param connectionState      the connection state to work with
	 * @return the created analyzer
	 */
	public ModelConnectionAnalyzer create(//
			IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, //
			RecommendationState recommendationState, ConnectionState connectionState) {
		return this.c.crea(graph, textExtractionState, modelExtractionState, recommendationState, connectionState);
	}

	private interface Crea {
		ModelConnectionAnalyzer crea(//
				IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, //
				RecommendationState recommendationState, ConnectionState connectionState);
	}
}
