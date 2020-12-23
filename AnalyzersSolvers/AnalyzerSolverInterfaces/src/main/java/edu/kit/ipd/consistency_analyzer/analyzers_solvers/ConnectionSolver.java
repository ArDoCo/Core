package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

/**
 * The connection solver creates connections between the recommendation state
 * and the extraction state.
 *
 * @author Sophie
 *
 */
public abstract class ConnectionSolver extends Solver implements IConnectionSolver {

	protected ITextExtractionState textExtractionState;
	protected IModelExtractionState modelExtractionState;
	protected IRecommendationState recommendationState;
	protected IConnectionState connectionState;

	/**
	 * Creates a new solver.
	 *
	 * @param dependencyType       the dependencies of the analyzer
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to look up
	 * @param connectionState      the connection state to work with
	 */
	protected ConnectionSolver(//
			DependencyType dependencyType, ITextExtractionState textExtractionState, //
			IModelExtractionState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState) {
		super(dependencyType);
		this.textExtractionState = textExtractionState;
		this.modelExtractionState = modelExtractionState;
		this.recommendationState = recommendationState;
		this.connectionState = connectionState;
	}
}
