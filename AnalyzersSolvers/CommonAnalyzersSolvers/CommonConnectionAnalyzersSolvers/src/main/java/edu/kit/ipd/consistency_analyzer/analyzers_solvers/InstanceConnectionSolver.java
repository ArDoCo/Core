package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendedInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

/**
 * This connector finds names of model instance in recommended instances.
 *
 * @author Sophie
 *
 */
@MetaInfServices(IConnectionSolver.class)
public class InstanceConnectionSolver extends ConnectionSolver {

	private double probability = GenericConnectionAnalyzerSolverConfig.INSTANCE_CONNECTION_SOLVER_PROBABILITY;
	private double probabilityWithoutType = GenericConnectionAnalyzerSolverConfig.INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE;

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
			ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, textExtractionState, modelExtractionState, recommendationState, connectionState);
	}

	public InstanceConnectionSolver() {
		this(null, null, null, null);
	}

	@Override
	public IConnectionSolver create(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState) {
		return new InstanceConnectionSolver(textExtractionState, modelExtractionState, recommendationState, connectionState);
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
		for (IInstance i : modelExtractionState.getInstances()) {
			List<IRecommendedInstance> mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(i, ris);

			List<IRecommendedInstance> mostLikelyRiWithoutType = mostLikelyRi.stream().filter(ri -> !ri.getTypeMappings().isEmpty()).collect(Collectors.toList());
			mostLikelyRiWithoutType.stream().forEach(ml -> connectionState.addToLinks(ml, i, probabilityWithoutType));
			mostLikelyRi.stream().forEach(ml -> connectionState.addToLinks(ml, i, probability));
		}
	}

}
