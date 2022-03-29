/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.Map;

import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedBags;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;

public class InitialInconsistencyAgent extends InconsistencyAgent {
    private static final double THRESHOLD_NAME_PROBABILITY = 0.5;
    private static final double THRESHOLD_TYPE_PROBABILITY = 0.9;

    private double threshold = 0.6d;

    private MutableSortedBag<Double> probabilities = SortedBags.mutable.empty();

    public InitialInconsistencyAgent() {
        // empty
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
        var recommendedInstances = inconsistencyState.getRecommendedInstances();

        threshold = analyzeProbabilitiesofRecommendedInstances(recommendedInstances);

        for (var recommendedInstance : recommendedInstances) {
            if (performHeuristicsAndChecks(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }

        inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    private double analyzeProbabilitiesofRecommendedInstances(MutableList<IRecommendedInstance> recommendedInstances) {
        var highestProbability = 0.0d;
        for (var recommendedInstance : recommendedInstances) {
            var probability = recommendedInstance.getProbability();
            if (probability > highestProbability) {
                highestProbability = probability;
            }
            if (logger.isDebugEnabled()) {
                var roundedProbability = Math.round(probability * 10.0) / 10.0;
                this.probabilities.addOccurrences(roundedProbability, 1);
            }
        }

        if (logger.isDebugEnabled()) {
            for (var prob : probabilities.toMapOfItemToCount().entrySet()) {
                logger.debug(prob.getKey() + ": " + prob.getValue());
            }
        }

        return highestProbability * 0.8;
    }

    private boolean performHeuristicsAndChecks(IRecommendedInstance recommendedInstance) {
        var checksArePositive = checkProbabilityOfBeingRecommendedInstance(recommendedInstance);
        checksArePositive = checksArePositive && checkProbabilitiesForNounMappingTypes(recommendedInstance);
        return checksArePositive && checkTwo(recommendedInstance);
    }

    private boolean checkProbabilityOfBeingRecommendedInstance(IRecommendedInstance recommendedInstance) {
        var probability = recommendedInstance.getProbability();
        return probability > threshold;
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

        // - more than one instance in the text for a recommendedInstance
        // - filter words / NounMappings that are common expressions
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }
}
