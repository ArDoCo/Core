package modelconnector.connectionGenerator.solvers;

import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This connector finds names of model instance in recommended instances.
 *
 * @author Sophie
 *
 */
public class InstanceConnectionSolver extends ModelConnectionSolver {

	private double probability = ModelConnectorConfiguration.instanceConnectionSolver_Probability;
	private double probabilityWithoutType = ModelConnectorConfiguration.instanceConnectionSolver_ProbabilityWithoutType;

	/**
	 * Creates a new InstanceMappingConnector.
	 *
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 * @param connectionState      the connection state
	 */
	public InstanceConnectionSolver(//
			IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState, ConnectionState connectionState) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, graph, textExtractionState, modelExtractionState, recommendationState, connectionState);
	}

	/**
	 * Executes the connector.
	 */
	@Override
	public void exec() {

		findNamesOfModelInstancesInSupposedMappings();
	}

	/**
	 * Seaches in the recommended instances of the recommendation state for similar
	 * names to extracted instances. If some are found the instance link is added to
	 * the connection state.
	 */
	private void findNamesOfModelInstancesInSupposedMappings() {
		List<RecommendedInstance> ris = recommendationState.getRecommendedInstances();
		for (Instance i : modelExtractionState.getInstances()) {
			List<RecommendedInstance> mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(i, ris);

			List<RecommendedInstance> mostLikelyRiWithoutType = mostLikelyRi.stream().filter(ri -> ri.getTypeMappings().size() != 0).collect(Collectors.toList());
			mostLikelyRiWithoutType.stream().forEach(ml -> connectionState.addToLinks(ml, i, probabilityWithoutType));
			mostLikelyRi.stream().forEach(ml -> connectionState.addToLinks(ml, i, probability));
		}
	}
}
