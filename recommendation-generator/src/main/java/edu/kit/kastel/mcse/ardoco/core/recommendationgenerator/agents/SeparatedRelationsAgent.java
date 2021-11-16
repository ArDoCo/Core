package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;
import edu.kit.kastel.mcse.ardoco.core.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.util.SimilarityUtils;

/**
 * The separated relation solver is a solver for the creation of recommended relations. Whenever a recommended instance
 * owes an occurrence with a separator a separator relation is recommended.
 *
 * @author Sophie
 *
 */
@MetaInfServices(RecommendationAgent.class)
public class SeparatedRelationsAgent extends RecommendationAgent {

    private double probability;
    private String relName = "separated";

    /**
     * Prototype constructor.
     */
    public SeparatedRelationsAgent() {
        super(GenericRecommendationConfig.class);
    }

    private SeparatedRelationsAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            GenericRecommendationConfig config) {
        super(GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
        probability = config.separatedRelationSolverProbility;
    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
            Configuration config) {
        return new SeparatedRelationsAgent(text, textState, modelExtractionState, recommendationState, (GenericRecommendationConfig) config);
    }

    /**
     * Executes the solver
     */
    @Override
    public void exec() {
        addRelationsForAllRecommendedInstances();
    }

    /**
     * Searches for recommended instances with a separator in one of their occurrences. For each part of the splitted
     * occurrences different matchings for the end points of the relation are possible. Therefore, the carthesian
     * product is created. For each combination a possible recommendation is created.
     */
    private void addRelationsForAllRecommendedInstances() {
        for (IRecommendedInstance recommendedInstance : recommendationState.getRecommendedInstances()) {

            ImmutableList<String> occurrencesWithSeparator = collectOccurrencesWithSeparators(recommendedInstance);

            for (String occurrence : occurrencesWithSeparator) {
                buildRelation(getAllCorrespondingRecommendationsForParticipants(recommendedInstance, occurrence));
            }

        }
    }

    private static ImmutableList<String> collectOccurrencesWithSeparators(IRecommendedInstance recommendedInstance) {
        ImmutableList<String> occs = collectOccurrencesAsStrings(recommendedInstance);
        return occs.select(CommonUtilities::containsSeparator);

    }

    private static ImmutableList<String> collectOccurrencesAsStrings(IRecommendedInstance recInstance) {
        MutableList<String> occurrences = Lists.mutable.empty();
        for (INounMapping nnm : recInstance.getNameMappings()) {
            occurrences.addAll(nnm.getSurfaceForms().castToCollection());
        }
        return occurrences.toImmutable();
    }

    private ImmutableList<ImmutableList<IRecommendedInstance>> getAllCorrespondingRecommendationsForParticipants(IRecommendedInstance recInstance,
            String occurrence) {
        String recInstanceName = recInstance.getName();

        ImmutableList<String> relationParticipants = CommonUtilities.splitAtSeparators(occurrence);

        MutableList<MutableList<IRecommendedInstance>> participatingRecInstances = Lists.mutable.empty();

        for (var i = 0; i < relationParticipants.size(); i++) {
            String participant = relationParticipants.get(i);
            participatingRecInstances.add(Lists.mutable.empty());

            if (SimilarityUtils.areWordsSimilar(recInstanceName, participant)) {
                participatingRecInstances.get(i).add(recInstance);
                continue;
            }

            participatingRecInstances.get(i).addAll(recommendationState.getRecommendedInstancesBySimilarName(participant).castToCollection());
        }
        return participatingRecInstances.collect(MutableList::toImmutable).toImmutable();
    }

    private void buildRelation(ImmutableList<ImmutableList<IRecommendedInstance>> recommendedParticipants) {
        if (recommendedParticipants.size() < 2) {
            return;
        }

        ImmutableList<ImmutableList<IRecommendedInstance>> allRelationProbabilities = CommonUtilities.cartesianProduct(Lists.immutable.empty(),
                recommendedParticipants);

        for (ImmutableList<IRecommendedInstance> p : allRelationProbabilities) {
            MutableList<IRecommendedInstance> possibility = p.toList();
            IRecommendedInstance r1 = possibility.get(0);
            IRecommendedInstance r2 = possibility.get(1);
            possibility.remove(r1);
            possibility.remove(r2);

            recommendationState.addRecommendedRelation(relName, r1, r2, Lists.immutable.withAll(possibility), probability, Lists.immutable.empty());
        }

    }
}
