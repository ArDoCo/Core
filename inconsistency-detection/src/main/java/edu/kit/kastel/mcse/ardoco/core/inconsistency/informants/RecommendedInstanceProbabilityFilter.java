/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import java.util.Map;

import org.eclipse.collections.api.bag.sorted.MutableSortedBag;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedBags;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;

/**
 * Filters {@link RecommendedInstance}s that have low probabilities of being an entity. This can either be because the
 * probability of being a {@link RecommendedInstance} is low or because the probability of having a mapping for a name
 * and/or type is low.
 *
 *
 */
public class RecommendedInstanceProbabilityFilter extends Filter {
    @Configurable
    private double thresholdNameAndTypeProbability = 0.3;
    @Configurable
    private double thresholdNameOrTypeProbability = 0.8;

    @Configurable
    private double threshold = 0.5d;

    @Configurable
    private boolean dynamicThreshold = true;
    @Configurable
    private double dynamicThresholdFactor = 0.7;

    private MutableSortedBag<Double> probabilities = SortedBags.mutable.empty();

    public RecommendedInstanceProbabilityFilter(DataRepository dataRepository) {
        super(RecommendedInstanceProbabilityFilter.class.getSimpleName(), dataRepository);
    }

    /**
     * Filter RecommendedInstances based on various heuristics. First, filter unlikely ones (low probability).
     */
    @Override
    protected void filterRecommendedInstances(InconsistencyState inconsistencyState) {
        var filteredRecommendedInstances = Lists.mutable.<RecommendedInstance>empty();
        var recommendedInstances = inconsistencyState.getRecommendedInstances();

        if (dynamicThreshold) {
            var highestProbability = analyzeProbabilitiesofRecommendedInstances(recommendedInstances);
            this.threshold = dynamicThresholdFactor * highestProbability;
        }

        logger.debug("Threshold for RecommendedInstances: {}", threshold);

        for (var recommendedInstance : recommendedInstances) {
            if (performHeuristicsAndChecks(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }

        inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    private double analyzeProbabilitiesofRecommendedInstances(MutableList<RecommendedInstance> recommendedInstances) {
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

    private boolean performHeuristicsAndChecks(RecommendedInstance recommendedInstance) {
        var checksArePositive = checkProbabilityOfBeingRecommendedInstance(recommendedInstance);
        return checksArePositive && checkProbabilitiesForNounMappingTypes(recommendedInstance);
    }

    private boolean checkProbabilityOfBeingRecommendedInstance(RecommendedInstance recommendedInstance) {
        var probability = recommendedInstance.getProbability();
        return probability > threshold;
    }

    /**
     * Check for probabilities of the {@link NounMapping}s that are contained by the {@link RecommendedInstance}. If
     * they exceed a threshold, then the check is positive. The {@link RecommendedInstance} needs to either be certain
     * for name or type or decently certain for name and type.
     *
     * @param recommendedInstance the {@link RecommendedInstance} to check
     * @return true if the probabilities of the types exceed a threshold
     */
    private boolean checkProbabilitiesForNounMappingTypes(RecommendedInstance recommendedInstance) {
        var highestTypeProbability = getHighestTypeProbability(recommendedInstance.getTypeMappings());
        var highestNameProbability = getHighestNameProbability(recommendedInstance.getTypeMappings());

        return highestTypeProbability > thresholdNameAndTypeProbability && highestNameProbability > thresholdNameAndTypeProbability || highestTypeProbability > thresholdNameOrTypeProbability || highestNameProbability > thresholdNameOrTypeProbability;

    }

    private double getHighestNameProbability(ImmutableList<NounMapping> nounMappings) {
        if (nounMappings.isEmpty()) {
            return 0.0;
        }
        return nounMappings.collect(NounMapping::getProbability).max();
    }

    private double getHighestTypeProbability(ImmutableList<NounMapping> typeMappings) {
        if (typeMappings.isEmpty()) {
            return 0.0;
        }
        return typeMappings.collect(NounMapping::getProbability).max();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }

}
