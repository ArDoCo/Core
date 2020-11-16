package modelconnector.recommendationGenerator.analyzers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Factory class for the creation of recommendation analyzers.
 *
 * @author Sophie
 *
 */
public enum RecommendationAnalyzerType {

	/**
	 * Redirection of the creation of a name type analyzer.
	 */
	NAME_TYPE_ANALYZER(NameTypeAnalyzer::new),

	/**
	 * Redirection of the creation of an extractino dependent occurrence analyzer.
	 */
	EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER(ExtractionDependentOccurrenceAnalyzer::new),

	/**
	 * Redirection of the creation of a extracted terms analyzer.
	 */
	EXTRACTED_TERMS_ANALYZER(ExtractedTermsAnalyzer::new);

	private Crea c;

	RecommendationAnalyzerType(Crea c) {
		this.c = c;
	}

	/**
	 * Creates a new analyzer.
	 *
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to work with
	 * @return the created analyzer
	 */
	public RecommendationAnalyzer create(IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		return this.c.crea(graph, textExtractionState, modelExtractionState, recommendationState);
	}

	private interface Crea {
		RecommendationAnalyzer crea(IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState);
	}

}
