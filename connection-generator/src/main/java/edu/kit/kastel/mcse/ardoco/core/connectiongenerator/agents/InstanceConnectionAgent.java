/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * This connector finds names of model instance in recommended instances.
 *
 * @author Sophie
 */
public class InstanceConnectionAgent extends ConnectionAgent {

    @Configurable
    private double probability = 1.0;
    @Configurable
    private double probabilityWithoutType = 0.8;

    /**
     * Create the agent.
     */
    public InstanceConnectionAgent() {

    }

    /**
     * Executes the connector.
     */
    @Override
    public void execute(ConnectionAgentData data) {
        for (var model : data.getModelIds()) {
            var modelState = data.getModelState(model);
            var recommendationState = data.getRecommendationState(modelState.getMetamodel());
            var connectionState = data.getConnectionState(model);
            findNamesOfModelInstancesInSupposedMappings(modelState, recommendationState, connectionState);
            createLinksForEqualOrSimilarRecommendedInstances(modelState, recommendationState, connectionState);
        }
    }

    /**
     * Searches in the recommended instances of the recommendation state for similar names to extracted instances. If
     * some are found the instance link is added to the connection state.
     *
     * @param modelState
     * @param recommendationState
     * @param connectionState
     */
    private void findNamesOfModelInstancesInSupposedMappings(IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState) {
        ImmutableList<IRecommendedInstance> ris = recommendationState.getRecommendedInstances();
        for (IModelInstance i : modelState.getInstances()) {
            ImmutableList<IRecommendedInstance> mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(i, ris);

            for (var ri : mostLikelyRi) {
                var riProbability = ri.getTypeMappings().isEmpty() ? probabilityWithoutType : probability;
                connectionState.addToLinks(ri, i, riProbability);
            }
        }
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState) {
        for (var ri : recommendationState.getRecommendedInstances()) {
            var sameInstances = modelState.getInstances().select(i -> SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(ri, i));
            sameInstances.forEach(i -> connectionState.addToLinks(ri, i, probability));
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
