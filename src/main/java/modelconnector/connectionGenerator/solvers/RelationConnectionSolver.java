package modelconnector.connectionGenerator.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.connectionGenerator.state.InstanceLink;
import modelconnector.connectionGenerator.state.RelationLink;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 *
 * The relation connection solver searches for relations of the model extraction state and the recommendation state. If
 * similar relations are found a relation link is created in the connection state.
 *
 * @author Sophie
 *
 */
public class RelationConnectionSolver extends ModelConnectionSolver {

    private double probability = ModelConnectorConfiguration.RELATION_CONNECTION_SOLVER_PROBABILITY;

    /**
     * Creates a new RelationConenctionSolver.
     *
     * @param graph
     *            the PARSE graph
     * @param textExtractionState
     *            the text extraction state
     * @param modelExtractionState
     *            the model extraction state
     * @param recommendationState
     *            the recommendation state
     * @param connectionState
     *            the connection state
     */
    public RelationConnectionSolver(//
            IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState,
            RecommendationState recommendationState, ConnectionState connectionState) {
        super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, graph, textExtractionState, modelExtractionState,
                recommendationState, connectionState);
    }

    @Override
    public void exec() {
        solveReferencesOfRelations();

    }

    /**
     * Searches for relations of the modelExtractionState in recommended relations of the recommendationState. If a
     * relation with the same order of similar instances can be found a relationLink is created.
     */
    private void solveReferencesOfRelations() {

        for (Relation relation : modelExtractionState.getRelations()) {
            List<RecommendedRelation> similarRecommendedRelations = new ArrayList<>();

            for (RecommendedRelation recommendedRelation : recommendationState.getRecommendedRelations()) {

                int relationSize = relation.getInstances()
                                           .size();
                int recommendedRelationSize = recommendedRelation.getRelationInstances()
                                                                 .size();
                if (relationSize != recommendedRelationSize) {
                    continue;
                }

                List<List<RecommendedInstance>> possibilities = getPossibilities(relation, recommendedRelation);

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

    private List<List<RecommendedInstance>> getPossibilitesBackwards(Relation relation,
            RecommendedRelation recommendedRelation) {
        List<List<RecommendedInstance>> possibilities = new ArrayList<>();
        int relationSize = relation.getInstances()
                                   .size();
        for (int i = 0; i < relationSize; i++) {
            Instance relationInstance = relation.getInstances()
                                                .get(i);
            int indexForRecommendedRelation = relationSize - 1 - i;
            RecommendedInstance recommendedRelationInstance = recommendedRelation.getRelationInstances()
                                                                                 .get(indexForRecommendedRelation);
            List<RecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(
                    relationInstance, List.of(recommendedRelationInstance));

            if (possibility.isEmpty()) {
                break; // TODO check if this break is necessary
            } else {
                possibilities.add(possibility);
            }
        }

        return possibilities;
    }

    private List<List<RecommendedInstance>> getPossibilities(Relation relation,
            RecommendedRelation recommendedRelation) {
        List<List<RecommendedInstance>> possibilities = new ArrayList<>();
        int relationSize = relation.getInstances()
                                   .size();
        for (int i = 0; i < relationSize; i++) {
            Instance relationInstance = relation.getInstances()
                                                .get(i);
            RecommendedInstance recommendedRelationInstance = recommendedRelation.getRelationInstances()
                                                                                 .get(i);
            List<RecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(
                    relationInstance, List.of(recommendedRelationInstance));

            if (possibility.isEmpty()) {
                break; // TODO check if this break is necessary
            } else {
                possibilities.add(possibility);
            }
        }
        return possibilities;
    }

    private void addRelationIfInstanceInConnectionState(List<RecommendedRelation> similarRecommendedRelations,
            Relation relation) {

        Predicate<? super RecommendedRelation> filterPredicate = similarRecommendedInstance -> similarRecommendedInstance.getRelationInstances()
                                                                         .stream()
                                                                         .anyMatch(recommendedInstance -> {
                                                                             List<InstanceLink> instanceLinksByRecommendedInstance = connectionState.getInstanceLinksByRecommendedInstance(
                                                                                     recommendedInstance);
                                                                             return !instanceLinksByRecommendedInstance.isEmpty();
                                                                         });
        List<RecommendedRelation> similarRecommendedRelations2 = similarRecommendedRelations.stream()
                                                                                            .filter(filterPredicate)
                                                                                            .collect(
                                                                                                    Collectors.toList());

        for (RecommendedRelation similarReRelation : similarRecommendedRelations2) {

            connectionState.addToLinks(
                    new RelationLink(similarReRelation, relation, probability / similarRecommendedRelations2.size()));
        }
    }
}
