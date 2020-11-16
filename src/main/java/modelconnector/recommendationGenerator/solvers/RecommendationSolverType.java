package modelconnector.recommendationGenerator.solvers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Factory class for the creation of recommendation solvers.
 *
 * @author Sophie
 *
 */
public enum RecommendationSolverType {

	/**
	 * Redirection of the creation of a reference solver.
	 */
	REFERENCE_SOLVER(ReferenceSolver::new),

	/**
	 * Redirection of the creation of a separated relations solver.
	 */
	SEPARATED_RELATION_SOLVER(SeparatedRelationsSolver::new);

	private Crea c;

	RecommendationSolverType(Crea c) {
		this.c = c;
	}

	/**
	 * Creates a new solver.
	 *
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to work with
	 * @return the created solver
	 */
	public RecommendationSolver create(IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		return this.c.crea(graph, textExtractionState, modelExtractionState, recommendationState);
	}

	private interface Crea {
		RecommendationSolver crea(IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState);
	}

}
