package modelconnector.connectionGenerator.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.connectionGenerator.state.RelationLink;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 *
 * The relation connection solver searches for relations of the model extraction
 * state and the recommendation state. If similar relations are found a relation
 * link is created in the connection state.
 *
 * @author Sophie
 *
 */
public class RelationConnectionSolver extends ModelConnectionSolver {

	private double probability = ModelConnectorConfiguration.relationConnectionSolver_Probability;

	/**
	 * Creates a new RelationConenctionSolver.
	 *
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 * @param connectionState      the connection state
	 */
	public RelationConnectionSolver(//
			IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState, ConnectionState connectionState) {
		super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, graph, textExtractionState, modelExtractionState, recommendationState, connectionState);
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

		for (Relation relation : modelExtractionState.getRelations()) {
			List<RecommendedRelation> similarRecommendedRelations = new ArrayList<>();

			for (RecommendedRelation reRelation : recommendationState.getRecommendedRelations()) {

				if (relation.getInstances().size() != reRelation.getRelationInstances().size()) {
					continue;
				}

				List<List<RecommendedInstance>> possibilities = new ArrayList<>();
				for (int i = 0; i < relation.getInstances().size(); i++) {
					List<RecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(//
							relation.getInstances().get(i), List.of(reRelation.getRelationInstances().get(i)));
					if (possibility.isEmpty()) {
						break;
					} else {
						possibilities.add(possibility);
					}
				}

				if (possibilities.size() == relation.getInstances().size()) {
					similarRecommendedRelations.add(reRelation);
				} else {
					for (int i = 0; i < relation.getInstances().size(); i++) {
						List<RecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(//
								relation.getInstances().get(i), List.of(reRelation.getRelationInstances().get(relation.getInstances().size() - 1 - i)));
						if (possibility.isEmpty()) {
							break;
						} else {
							possibilities.add(possibility);
						}

					}

					if (possibilities.size() == relation.getInstances().size()) {
						similarRecommendedRelations.add(reRelation);
					}
				}

			}

			addRelationIfInstanceInConnectionState(similarRecommendedRelations, relation);

		}

	}

	private void addRelationIfInstanceInConnectionState(List<RecommendedRelation> similarRecommendedRelations, Relation relation) {

		List<RecommendedRelation> similarRecommendedRelations2 = similarRecommendedRelations.stream().filter(//
				si -> si.getRelationInstances().stream().anyMatch(//
						ri -> !connectionState.getInstanceLinksByRecommendedInstance(ri).isEmpty()))
				.collect(Collectors.toList());

		for (RecommendedRelation similarReRelation : similarRecommendedRelations2) {

			this.connectionState.addToLinks(new RelationLink(similarReRelation, relation, probability / similarRecommendedRelations2.size()));
		}
	}
}
