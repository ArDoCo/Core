package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.ConnectionAgent;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
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

	// Required for the service loader
	public InstanceConnectionAgent() {
		super(GenericConnectionAnalyzerSolverConfig.class);
	}

	private InstanceConnectionAgent(//
			IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState, GenericConnectionAnalyzerSolverConfig config) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, GenericConnectionAnalyzerSolverConfig.class, text, textState, modelState, recommendationState, connectionState);
		probability = config.instanceConnectionSolverProbability;
		probabilityWithoutType = config.instanceConnectionSolverProbabilityWithoutType;
	}

	@Override
	public ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState, Configuration config) {
		return new InstanceConnectionAgent(text, textState, modelState, recommendationState, connectionState, (GenericConnectionAnalyzerSolverConfig) config);
	}

	/**
	 * Executes the connector.
	 */
	@Override
	public void exec() {
		findNamesOfModelInstancesInSupposedMappings();
	}

	/**
	 * Seaches in the recommended instances of the recommendation state for similar names to extracted instances. If
	 * some are found the instance link is added to the connection state.
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
