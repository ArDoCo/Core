package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents.ConnectionAgent;
import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendedInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

/**
 * This connector finds names of model instance in recommended instances.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionAgent.class)
public class InstanceConnectionAgent extends ConnectionAgent {

	private double probability;
	private double probabilityWithoutType;

	/**
	 * Creates a new InstanceMappingConnector.
	 *
	 * @param graph               the PARSE graph
	 * @param textState           the text extraction state
	 * @param modelState          the model extraction state
	 * @param recommendationState the recommendation state
	 * @param connectionState     the connection state
	 */
	public InstanceConnectionAgent(//
			IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, text, textState, modelState, recommendationState, connectionState);
		probability = GenericConnectionAnalyzerSolverConfig.INSTANCE_CONNECTION_SOLVER_PROBABILITY;
		probabilityWithoutType = GenericConnectionAnalyzerSolverConfig.INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE;
	}

	// Required for the service loader
	public InstanceConnectionAgent() {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION);
	}

	public InstanceConnectionAgent(AgentDatastructure data) {
		this(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState());
	}

	public InstanceConnectionAgent(IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState,
			double probability, double probabilityWithoutType) {
		this(text, textExtractionState, modelExtractionState, recommendationState, connectionState);
		this.probability = probability;
		this.probabilityWithoutType = probabilityWithoutType;
	}

	@Override
	public ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState) {
		return new InstanceConnectionAgent(text, textState, modelState, recommendationState, connectionState);
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
		List<IRecommendedInstance> ris = recommendationState.getRecommendedInstances();
		for (IInstance i : modelState.getInstances()) {
			List<IRecommendedInstance> mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(i, ris);

			List<IRecommendedInstance> mostLikelyRiWithoutType = mostLikelyRi.stream().filter(ri -> !ri.getTypeMappings().isEmpty()).collect(Collectors.toList());
			mostLikelyRiWithoutType.stream().forEach(ml -> connectionState.addToLinks(ml, i, probabilityWithoutType));
			mostLikelyRi.stream().forEach(ml -> connectionState.addToLinks(ml, i, probability));
		}
	}

}
