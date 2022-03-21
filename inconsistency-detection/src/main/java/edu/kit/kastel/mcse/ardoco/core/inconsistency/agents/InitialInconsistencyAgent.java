/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;

public class InitialInconsistencyAgent extends InconsistencyAgent {
    private static final double THRESHOLD_NAME_PROBABILITY = 0.5;
    private static final double THRESHOLD_TYPE_PROBABILITY = 0.9;
    private static final double THRESHOLD_RI_PROBABILITY = 0.75d;

    public InitialInconsistencyAgent() {
    }

    @Override
    public void execute(InconsistencyAgentData data) {
        for (var model : data.getModelIds()) {
            var inconsistencyState = data.getInconsistencyState(model);
            var recommendationState = data.getRecommendationState(data.getModelState(model).getMetamodel());
            inconsistencyState.addRecommendedInstances(recommendationState.getRecommendedInstances().toList());
            filterRecommendedInstances(inconsistencyState);
        }
    }

    /**
     * Filter RecommendedInstances based on various heuristics. First, filter unlikely ones (low probability).
     */
    private void filterRecommendedInstances(IInconsistencyState inconsistencyState) {
        var filteredRecommendedInstances = Lists.mutable.<IRecommendedInstance> empty();
        for (var recommendedInstance : inconsistencyState.getRecommendedInstances()) {
            if (performHeuristicsAndChecks(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }

        inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    private boolean performHeuristicsAndChecks(IRecommendedInstance recommendedInstance) {
        var checksArePositive = checkProbabilityOfBeingRecommendedInstance(recommendedInstance);
        checksArePositive = checksArePositive && checkProbabilitiesForNounMappingTypes(recommendedInstance);
        return checksArePositive && checkTwo(recommendedInstance);
    }

    private boolean checkProbabilityOfBeingRecommendedInstance(IRecommendedInstance recommendedInstance) {
        return recommendedInstance.getProbability() > 0.3;
    }

    /**
     * Check for probabilities of the types for the {@link INounMapping}s that are contained by the
     * {@link IRecommendedInstance}. If they exceed a threshold, then the check is positive
     *
     * @param recommendedInstance the {@link IRecommendedInstance} to check
     * @return true if the probabilities of the types exceed a threshold
     */
    private boolean checkProbabilitiesForNounMappingTypes(IRecommendedInstance recommendedInstance) {
        // for (var type : recommendedInstance.getTypeMappings()) {
        // if (type.getProbabilityForType() > THRESHOLD_TYPE_PROBABILITY) {
        // return true;
        // }
        // }
        //
        // for (var name : recommendedInstance.getNameMappings()) {
        // if (name.getProbabilityForName() > THRESHOLD_NAME_PROBABILITY) {
        // return true;
        // }
        // }
        // return false;
        return true;
    }

    /**
     * Check for ???
     *
     * @param recommendedInstance the {@link IRecommendedInstance} to check
     */
    private boolean checkTwo(IRecommendedInstance recommendedInstance) {
        // TODO
        return true;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
