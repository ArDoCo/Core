/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

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
        findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances();
    }

    /**
     * Searches for instances mentioned in the text extraction state as names. If it founds some similar names it
     * creates recommendations.
     */
    private void findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances() {
        for (IModelInstance instance : modelState.getInstances()) {
            ImmutableList<INounMapping> similarToInstanceMappings = getSimilarNounMappings(instance);

            for (INounMapping similarNameMapping : similarToInstanceMappings) {
                recommendationState.addRecommendedInstance(similarNameMapping.getReference(), probability, similarToInstanceMappings);
            }
        }

    }

    private ImmutableList<INounMapping> getSimilarNounMappings(IModelInstance instance) {
        return textState.getNames().select(nounMapping -> SimilarityUtils.isNounMappingSimilarToModelInstance(nounMapping, instance));
    }

}
