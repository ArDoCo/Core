package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

/**
 *
 * The relation connection solver searches for relations of the model extraction state and the recommendation state. If
 * similar relations are found a relation link is created in the connection state.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionAgent.class)
public class RelationConnectionAgent extends ConnectionAgent {

    private double probability;

    /**
     * Create the agent.
     */
    public RelationConnectionAgent() {
        super(GenericConnectionConfig.class);
    }

    private RelationConnectionAgent(//
            IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, GenericConnectionConfig config) {
        super(GenericConnectionConfig.class, text, textExtractionState, modelExtractionState, recommendationState, connectionState);
        probability = config.relationConnectionSolverProbability;
    }

    @Override
    public RelationConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new RelationConnectionAgent(text, textState, modelState, recommendationState, connectionState, (GenericConnectionConfig) config);
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

        for (IModelRelation relation : modelState.getRelations()) {
            MutableList<IRecommendedRelation> similarRecommendedRelations = Lists.mutable.empty();

            for (IRecommendedRelation recommendedRelation : recommendationState.getRecommendedRelations()) {

                int relationSize = relation.getInstances().size();
                int recommendedRelationSize = recommendedRelation.getRelationInstances().size();
                if (relationSize != recommendedRelationSize) {
                    continue;
                }

                MutableList<ImmutableList<IRecommendedInstance>> possibilities = getPossibilities(relation, recommendedRelation).toList();

                if (possibilities.size() == relationSize) {
                    similarRecommendedRelations.add(recommendedRelation);
                } else {
                    possibilities.addAll(getPossibilitesBackwards(relation, recommendedRelation).castToCollection());

                    if (possibilities.size() == relationSize) {
                        similarRecommendedRelations.add(recommendedRelation);
                    }
                }

            }

            addRelationIfInstanceInConnectionState(similarRecommendedRelations.toImmutable(), relation);

        }

    }

    private ImmutableList<ImmutableList<IRecommendedInstance>> getPossibilitesBackwards(IModelRelation relation, IRecommendedRelation recommendedRelation) {
        MutableList<ImmutableList<IRecommendedInstance>> possibilities = Lists.mutable.empty();
        int relationSize = relation.getInstances().size();
        for (var i = 0; i < relationSize; i++) {
            IModelInstance relationInstance = relation.getInstances().get(i);
            int indexForRecommendedRelation = relationSize - 1 - i;
            IRecommendedInstance recommendedRelationInstance = recommendedRelation.getRelationInstances().get(indexForRecommendedRelation);
            ImmutableList<IRecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(relationInstance,
                    Lists.immutable.with(recommendedRelationInstance));

            if (possibility.isEmpty()) {
                break;

            }
            possibilities.add(possibility);

        }

        return possibilities.toImmutable();
    }

    private ImmutableList<ImmutableList<IRecommendedInstance>> getPossibilities(IModelRelation relation, IRecommendedRelation recommendedRelation) {
        MutableList<ImmutableList<IRecommendedInstance>> possibilities = Lists.mutable.empty();
        int relationSize = relation.getInstances().size();
        for (var i = 0; i < relationSize; i++) {
            IModelInstance relationInstance = relation.getInstances().get(i);
            IRecommendedInstance recommendedRelationInstance = recommendedRelation.getRelationInstances().get(i);
            ImmutableList<IRecommendedInstance> possibility = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(relationInstance,
                    Lists.immutable.with(recommendedRelationInstance));

            if (possibility.isEmpty()) {
                break;
            }
            possibilities.add(possibility);

        }
        return possibilities.toImmutable();
    }

    private void addRelationIfInstanceInConnectionState(ImmutableList<IRecommendedRelation> similarRecommendedRelations, IModelRelation relation) {

        Predicate<IRecommendedRelation> filterPredicate = similarRecommendedInstance -> similarRecommendedInstance.getRelationInstances()
                .anySatisfy(recommendedInstance -> {
                    ImmutableList<IInstanceLink> instanceLinksByRecommendedInstance = connectionState
                            .getInstanceLinksByRecommendedInstance(recommendedInstance);
                    return !instanceLinksByRecommendedInstance.isEmpty();
                });

        ImmutableList<IRecommendedRelation> similarRecommendedRelations2 = similarRecommendedRelations.select(filterPredicate);

        for (IRecommendedRelation similarReRelation : similarRecommendedRelations2) {
            connectionState.addToLinks(similarReRelation, relation, probability / similarRecommendedRelations2.size());
        }
    }

}
