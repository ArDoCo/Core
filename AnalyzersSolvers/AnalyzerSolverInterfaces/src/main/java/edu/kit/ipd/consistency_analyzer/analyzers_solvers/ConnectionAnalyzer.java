package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

/**
 * This class represents an analyzer, that works on the level of the connection
 * creation.
 *
 * @author Sophie
 *
 */
public abstract class ConnectionAnalyzer extends Analyzer implements IConnectionAnalyzer {

	protected ITextExtractionState textExtractionState;
	protected IModelExtractionState modelExtractionState;
	protected IRecommendationState recommendationState;
	protected IConnectionState connectionState;

	/**
	 * Creates a new analyzer.
	 *
	 * @param dependencyType       the dependencies of the analyzer
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to look up
	 * @param connectionState      the connection state to work with
	 */
	protected ConnectionAnalyzer(DependencyType dependencyType, ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, //
			IRecommendationState recommendationState, IConnectionState connectionState) {
		super(dependencyType);
		this.textExtractionState = textExtractionState;
		this.modelExtractionState = modelExtractionState;
		this.recommendationState = recommendationState;
		this.connectionState = connectionState;
	}
}
