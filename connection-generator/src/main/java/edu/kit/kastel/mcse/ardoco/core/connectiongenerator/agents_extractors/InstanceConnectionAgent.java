package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors;

import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

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
			IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState,
			GenericConnectionAnalyzerSolverConfig config) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, GenericConnectionAnalyzerSolverConfig.class, text, textState, modelState, recommendationState,
				connectionState);
		probability = config.instanceConnectionSolverProbability;
		probabilityWithoutType = config.instanceConnectionSolverProbabilityWithoutType;
	}

	@Override
	public ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
			IConnectionState connectionState, Configuration config) {
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
	 * Seaches in the recommended instances of the recommendation state for similar
	 * names to extracted instances. If some are found the instance link is added to
	 * the connection state.
	 */
	private void findNamesOfModelInstancesInSupposedMappings() {
		List<IRecommendedInstance> ris = recommendationState.getRecommendedInstances();
		for (IInstance i : modelState.getInstances()) {
			List<IRecommendedInstance> mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(i, ris);

			List<IRecommendedInstance> mostLikelyRiWithoutType = mostLikelyRi.stream().filter(ri -> !ri.getTypeMappings().isEmpty())
					.collect(Collectors.toList());
			mostLikelyRiWithoutType.stream().forEach(ml -> connectionState.addToLinks(ml, i, probabilityWithoutType));
			mostLikelyRi.stream().forEach(ml -> connectionState.addToLinks(ml, i, probability));
		}
	}

}
