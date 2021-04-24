package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

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

	public RelationConnectionAgent() {
		super(GenericConnectionAnalyzerSolverConfig.class);
	}

	private RelationConnectionAgent(//
			IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
			IConnectionState connectionState, GenericConnectionAnalyzerSolverConfig config) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, GenericConnectionAnalyzerSolverConfig.class, text, textExtractionState, modelExtractionState,
				recommendationState, connectionState);
		probability = config.relationConnectionSolverProbability;
	}

	@Override
	public RelationConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
			IConnectionState connectionState, Configuration config) {
		return new RelationConnectionAgent(text, textState, modelState, recommendationState, connectionState, (GenericConnectionAnalyzerSolverConfig) config);
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
			List<IRecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(relationInstance,
					List.of(recommendedRelationInstance));

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
			List<IRecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(relationInstance,
					List.of(recommendedRelationInstance));

			if (possibility.isEmpty()) {
				break;
			}
			possibilities.add(possibility);

		}
		return possibilities;
	}

	private void addRelationIfInstanceInConnectionState(List<IRecommendedRelation> similarRecommendedRelations, IRelation relation) {

		Predicate<? super IRecommendedRelation> filterPredicate = similarRecommendedInstance -> similarRecommendedInstance.getRelationInstances().stream()
				.anyMatch(recommendedInstance -> {
					List<IInstanceLink> instanceLinksByRecommendedInstance = connectionState.getInstanceLinksByRecommendedInstance(recommendedInstance);
					return !instanceLinksByRecommendedInstance.isEmpty();
				});
		List<IRecommendedRelation> similarRecommendedRelations2 = similarRecommendedRelations.stream().filter(filterPredicate).collect(Collectors.toList());

		for (IRecommendedRelation similarReRelation : similarRecommendedRelations2) {
			connectionState.addToLinks(similarReRelation, relation, probability / similarRecommendedRelations2.size());
		}
	}

}
