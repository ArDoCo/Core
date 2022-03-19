/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The reference solver finds instances mentioned in the text extraction state as names. If it founds some similar names
 * it creates recommendations.
 *
 * @author Sophie
 */
public class ReferenceAgent extends ConnectionAgent {

    @Configurable
    private double probability = 0.75;

    /**
     * Create the agent.
     */
    public ReferenceAgent() {
    }

    /**
     * Executes the solver.
     */
    @Override
    public void execute(ConnectionAgentData data) {
        for (var model : data.getModelIds()) {
            var modelState = data.getModelState(model);
            var recommendationState = data.getRecommendationState(modelState.getMetamodel());
            var textState = data.getTextState();
            findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(modelState, recommendationState, textState);
        }
    }

    /**
     * Searches for instances mentioned in the text extraction state as names. If it founds some similar names it
     * creates recommendations.
     *
     * @param modelState
     * @param recommendationState
     * @param textState
     */
    private void findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(IModelState modelState, IRecommendationState recommendationState,
            ITextState textState) {
        for (IModelInstance instance : modelState.getInstances()) {
            ImmutableList<INounMapping> similarToInstanceMappings = getSimilarNounMappings(instance, textState);

            for (INounMapping similarNameMapping : similarToInstanceMappings) {
                recommendationState.addRecommendedInstance(similarNameMapping.getReference(), probability, similarToInstanceMappings);
            }
        }

    }

    private ImmutableList<INounMapping> getSimilarNounMappings(IModelInstance instance, ITextState textState) {
        return textState.getNames().select(nounMapping -> SimilarityUtils.isNounMappingSimilarToModelInstance(nounMapping, instance));
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
