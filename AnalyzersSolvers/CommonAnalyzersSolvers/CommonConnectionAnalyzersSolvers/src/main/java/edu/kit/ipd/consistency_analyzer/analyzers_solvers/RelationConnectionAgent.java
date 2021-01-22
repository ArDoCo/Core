package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents.ConnectionAgent;
import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstanceLink;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendedInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendedRelation;
import edu.kit.ipd.consistency_analyzer.datastructures.IRelation;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

/**
 *
 * The relation connection solver searches for relations of the model extraction
 * state and the recommendation state. If similar relations are found a relation
 * link is created in the connection state.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionAgent.class)
public class RelationConnectionAgent extends ConnectionAgent {

	private double probability;

	/**
	 * Creates a new RelationConenctionSolver.
	 *
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 * @param connectionState      the connection state
	 */
	public RelationConnectionAgent(//
			IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, text, textExtractionState, modelExtractionState, recommendationState, connectionState);
		probability = GenericConnectionAnalyzerSolverConfig.RELATION_CONNECTION_SOLVER_PROBABILITY;
	}

	public RelationConnectionAgent(//
			IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState, double probability) {
		this(text, textExtractionState, modelExtractionState, recommendationState, connectionState);
		this.probability = probability;
	}

	public RelationConnectionAgent() {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION);
	}

	public RelationConnectionAgent(AgentDatastructure data) {
		this(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState());
	}

	@Override
	public ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState) {
		return new RelationConnectionAgent(text, textState, modelState, recommendationState, connectionState);
	}

	@Override
	public void exec() {
		solveReferencesOfRelations();

	}

	/**
	 * Searches for relations of the modelExtractionState in recommended relations
	 * of the recommendationState. If a relation with the same order of similar
	 * instances can be found a relationLink is created.
	 */
	private void solveReferencesOfRelations() {

		for (IRelation relation : modelState.getRelations()) {
			List<IRecommendedRelation> similarRecommendedRelations = new ArrayList<>();

			for (IRecommendedRelation recommendedRelation : recommendationState.getRecommendedRelations()) {

				int relationSize = relation.getInstances().size();
				int recommendedRelationSize = recommendedRelation.getRelationInstances().size();
				if (relationSize != recommendedRelationSize) {
					continue;
				}

				List<List<IRecommendedInstance>> possibilities = getPossibilities(relation, recommendedRelation);

				if (possibilities.size() == relationSize) {
					similarRecommendedRelations.add(recommendedRelation);
				} else {
					possibilities.addAll(getPossibilitesBackwards(relation, recommendedRelation));

					if (possibilities.size() == relationSize) {
						similarRecommendedRelations.add(recommendedRelation);
					}
				}

			}

			addRelationIfInstanceInConnectionState(similarRecommendedRelations, relation);

		}

	}

	private List<List<IRecommendedInstance>> getPossibilitesBackwards(IRelation relation, IRecommendedRelation recommendedRelation) {
		List<List<IRecommendedInstance>> possibilities = new ArrayList<>();
		int relationSize = relation.getInstances().size();
		for (int i = 0; i < relationSize; i++) {
			IInstance relationInstance = relation.getInstances().get(i);
			int indexForRecommendedRelation = relationSize - 1 - i;
			IRecommendedInstance recommendedRelationInstance = recommendedRelation.getRelationInstances().get(indexForRecommendedRelation);
			List<IRecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(relationInstance, List.of(recommendedRelationInstance));

			if (possibility.isEmpty()) {
				break;

			}
			possibilities.add(possibility);

		}

		return possibilities;
	}

	private List<List<IRecommendedInstance>> getPossibilities(IRelation relation, IRecommendedRelation recommendedRelation) {
		List<List<IRecommendedInstance>> possibilities = new ArrayList<>();
		int relationSize = relation.getInstances().size();
		for (int i = 0; i < relationSize; i++) {
			IInstance relationInstance = relation.getInstances().get(i);
			IRecommendedInstance recommendedRelationInstance = recommendedRelation.getRelationInstances().get(i);
			List<IRecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(relationInstance, List.of(recommendedRelationInstance));

			if (possibility.isEmpty()) {
				break;
			}
			possibilities.add(possibility);

		}
		return possibilities;
	}

	private void addRelationIfInstanceInConnectionState(List<IRecommendedRelation> similarRecommendedRelations, IRelation relation) {

		Predicate<? super IRecommendedRelation> filterPredicate = similarRecommendedInstance -> similarRecommendedInstance.getRelationInstances().stream().anyMatch(recommendedInstance -> {
			List<IInstanceLink> instanceLinksByRecommendedInstance = connectionState.getInstanceLinksByRecommendedInstance(recommendedInstance);
			return !instanceLinksByRecommendedInstance.isEmpty();
		});
		List<IRecommendedRelation> similarRecommendedRelations2 = similarRecommendedRelations.stream().filter(filterPredicate).collect(Collectors.toList());

		for (IRecommendedRelation similarReRelation : similarRecommendedRelations2) {
			connectionState.addToLinks(similarReRelation, relation, probability / similarRecommendedRelations2.size());
		}
	}

}
