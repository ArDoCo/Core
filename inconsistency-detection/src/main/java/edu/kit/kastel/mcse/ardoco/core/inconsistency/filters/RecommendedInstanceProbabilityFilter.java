package edu.kit.kastel.mcse.ardoco.core.inconsistency.filters;

import java.util.Map;

import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedBags;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractFilter;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;

public class RecommendedInstanceProbabilityFilter extends AbstractFilter<InconsistencyAgentData> {
    private static final double THRESHOLD_NAME_AND_TYPE_PROBABILITY = 0.3;
    private static final double THRESHOLD_TYPE_OR_TYPE_PROBABILITY = 0.6;

    private double threshold = 0.4d;

    private MutableSortedBag<Double> probabilities = SortedBags.mutable.empty();

    @Override
    public void exec(InconsistencyAgentData data) {
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

        if (logger.isDebugEnabled()) {
            analyzeProbabilitiesofRecommendedInstances(recommendedInstances);
        }

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
                logger.debug("{}: {}", prob.getKey(), prob.getValue());
            }
            logger.debug("Highest probability: {}", highestProbability);
        }

        return highestProbability;
    }

    private boolean performHeuristicsAndChecks(IRecommendedInstance recommendedInstance) {
        var checksArePositive = checkProbabilityOfBeingRecommendedInstance(recommendedInstance);
        return checksArePositive && checkProbabilitiesForNounMappingTypes(recommendedInstance);
    }

    private boolean checkProbabilityOfBeingRecommendedInstance(IRecommendedInstance recommendedInstance) {
        var probability = recommendedInstance.getProbability();
        return probability > threshold;
    }

    /**
     * Check for probabilities of the {@link INounMapping}s that are contained by the {@link IRecommendedInstance}. If
     * they exceed a threshold, then the check is positive. The {@link IRecommendedInstance} needs to either be certain
     * for name or type or decently certain for name and type.
     *
     * @param recommendedInstance the {@link IRecommendedInstance} to check
     * @return true if the probabilities of the types exceed a threshold
     */
    private boolean checkProbabilitiesForNounMappingTypes(IRecommendedInstance recommendedInstance) {
        var highestTypeProbability = getHighestTypeProbability(recommendedInstance.getTypeMappings());
        var highestNameProbability = getHighestNameProbability(recommendedInstance.getTypeMappings());

        return highestTypeProbability > THRESHOLD_NAME_AND_TYPE_PROBABILITY && highestNameProbability > THRESHOLD_NAME_AND_TYPE_PROBABILITY
                || highestTypeProbability > THRESHOLD_TYPE_OR_TYPE_PROBABILITY || highestNameProbability > THRESHOLD_TYPE_OR_TYPE_PROBABILITY;

    }

    private double getHighestNameProbability(ImmutableList<INounMapping> nounMappings) {
        if (nounMappings.isEmpty()) {
            return 0.0;
        }
        return nounMappings.collect(INounMapping::getProbability).max();
    }

    private double getHighestTypeProbability(ImmutableList<INounMapping> typeMappings) {
        if (typeMappings.isEmpty()) {
            return 0.0;
        }
        return typeMappings.collect(INounMapping::getProbability).max();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }

}
