package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.util.SimilarityUtils;

/**
 * The reference solver finds instances mentioned in the text extraction state as names. If it founds some similar names
 * it creates recommendations.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionAgent.class)
public class ReferenceAgent extends ConnectionAgent {

    private double probability;
    private double areNamesSimilarThreshold;
    private double proportionalDecrease;

    /**
     * Create the agent.
     */
    public ReferenceAgent() {
        super(GenericConnectionConfig.class);
    }

    private ReferenceAgent(IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, GenericConnectionConfig config) {
        super(GenericConnectionConfig.class, text, textExtractionState, modelExtractionState, recommendationState, connectionState);
        probability = config.referenceSolverProbability;
        areNamesSimilarThreshold = config.referenceSolverAreNamesSimilarThreshold;
        proportionalDecrease = config.referenceSolverProportionalDecrease;
    }

    @Override
    public ConnectionAgent create(IText text, ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new ReferenceAgent(text, textState, modelExtractionState, recommendationState, connectionState, (GenericConnectionConfig) config);
    }

    /**
     * Executes the solver.
     */
    @Override
    public void exec() {

        solveReferencesOfNames();
    }

    /**
     * Searches for instances mentioned in the text extraction state as names. If it founds some similar names it
     * creates recommendations.
     */
    private void solveReferencesOfNames() {

        for (IModelInstance instance : modelState.getInstances()) {
            // ntrNodes mit Lemma ca. Name eines Modelelements

            ImmutableList<INounMapping> similarToInstanceMappings = //
                    textState.getNames()
                            .select(n -> SimilarityUtils.areWordsOfListsSimilar(//
                                    instance.getNames(), Lists.immutable.with(n.getReference()), areNamesSimilarThreshold));

            if (similarToInstanceMappings.isEmpty()) {

                solveReferenceOfNamesIfSimilarNameIsEmpty(instance);

            } else {

                for (INounMapping similarNameMapping : similarToInstanceMappings) {
                    recommendationState.addRecommendedInstanceJustName(similarNameMapping.getReference(), probability, similarToInstanceMappings);
                }
            }

        }

    }

    /**
     * Searches for the longest name of a given instance in the noun mappings of the text extraction state. If no
     * similar mapping can be found the search is continued. Otherwise, the found mapping is added to the recommendation
     * state.
     *
     * @param instance the current instance to find as noun mapping
     */
    private void solveReferenceOfNamesIfSimilarNameIsEmpty(IModelInstance instance) {
        ImmutableList<INounMapping> similarLongestNameMappings = textState.getNames()
                .select(nm -> SimilarityUtils.areWordsSimilar(instance.getLongestName(), nm.getReference()));

        if (similarLongestNameMappings.isEmpty()) {
            solveReferenceOfNamesIfNoSimilarLongNamesCouldBeFound(instance);
        } else if (similarLongestNameMappings.size() == 1) {
            recommendationState.addRecommendedInstanceJustName(similarLongestNameMappings.get(0).getReference(), probability, similarLongestNameMappings);
        }
    }

    /**
     * Searches for each name of the instance a similar mapping in the text extraction state. If some is found it is
     * added to the recommendation state. If its more than one the probability is decreased.
     *
     * @param instance the current instance to find as noun mapping
     */
    private void solveReferenceOfNamesIfNoSimilarLongNamesCouldBeFound(IModelInstance instance) {
        ImmutableList<INounMapping> similarNameMappings = Lists.immutable.with();
        // TODO @Sophie: This code seems to be strange .. because similarNameMappings will be overridden in the for loop
        // again and again ..
        for (String name : instance.getNames()) {
            similarNameMappings = textState.getNames().select(nm -> SimilarityUtils.areWordsSimilar(name, nm.getReference()));
        }

        double prob = probability;
        if (!similarNameMappings.isEmpty()) {
            prob = probability * proportionalDecrease;
        }

        for (INounMapping similarNameMapping : similarNameMappings) {
            recommendationState.addRecommendedInstanceJustName(similarNameMapping.getReference(), prob, similarNameMappings);
        }
    }

}
