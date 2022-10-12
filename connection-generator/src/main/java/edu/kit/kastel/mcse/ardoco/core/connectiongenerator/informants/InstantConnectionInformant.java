/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class InstantConnectionInformant extends Informant {
    @Configurable
    private double probability = 1.0;
    @Configurable
    private double probabilityWithoutType = 0.8;

    public InstantConnectionInformant(DataRepository dataRepository) {
        super(InstantConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(metamodel);
            var connectionState = connectionStates.getConnectionState(metamodel);

            findNamesOfModelInstancesInSupposedMappings(modelState, recommendationState, connectionState);
            createLinksForEqualOrSimilarRecommendedInstances(modelState, recommendationState, connectionState);
        }
    }

    /**
     * Searches in the recommended instances of the recommendation state for similar names to extracted instances. If
     * some are found the instance link is added to the connection state.
     */
    private void findNamesOfModelInstancesInSupposedMappings(ModelExtractionState modelState, RecommendationState recommendationState,
            ConnectionState connectionState) {
        var recommendedInstances = recommendationState.getRecommendedInstances();
        for (ModelInstance instance : modelState.getInstances()) {
            var mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(instance, recommendedInstances);

            for (var recommendedInstance : mostLikelyRi) {
                var riProbability = recommendedInstance.getTypeMappings().isEmpty() ? probabilityWithoutType : probability;
                connectionState.addToLinks(recommendedInstance, instance, this, riProbability);
            }
        }
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(ModelExtractionState modelState, RecommendationState recommendationState,
            ConnectionState connectionState) {
        for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
            var sameInstances = modelState.getInstances()
                    .select(instance -> SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstance, instance));
            sameInstances.forEach(instance -> connectionState.addToLinks(recommendedInstance, instance, this, probability));
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
