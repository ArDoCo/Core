package modelconnector.connectionGenerator.solvers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Factory class for the creation of model connection solvers.
 *
 * @author Sophie
 *
 */
public enum ModelConnectionSolverType {

	/**
	 * Redirection of the creation of an instance mapping connection solver.
	 */
	INSTANCE_CONNECTION_SOLVER(InstanceConnectionSolver::new),

	/**
	 * Redirection of the creation of a relation connection solver.
	 */
	RELATION_CONNECTION_SOLVER(RelationConnectionSolver::new);

	private Crea c;

	ModelConnectionSolverType(Crea c) {
		this.c = c;
	}

	/**
	 * Creates a new solver.
	 *
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to look up
	 * @param connectionState      the connection state to work with
	 * @return the created solver
	 */
	public ModelConnectionSolver create(//
			IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, //
			RecommendationState recommendationState, ConnectionState connectionState) {
		return this.c.crea(graph, textExtractionState, modelExtractionState, recommendationState, connectionState);
	}

	private interface Crea {
		ModelConnectionSolver crea(//
				IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, //
				RecommendationState recommendationState, ConnectionState connectionState);
	}

}
