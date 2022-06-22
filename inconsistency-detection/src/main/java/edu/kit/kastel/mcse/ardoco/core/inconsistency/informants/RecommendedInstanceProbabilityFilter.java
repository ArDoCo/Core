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
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * Filters {@link IRecommendedInstance}s that have low probabilities of being an entity. This can either be because the
 * probability of being a {@link IRecommendedInstance} is low or because the probability of having a mapping for a name
 * and/or type is low.
 *
 * @author Jan Keim
 *
 */
public class RecommendedInstanceProbabilityFilter extends Informant {
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
        super("RecommendedInstanceProbabilityFilter", dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);

        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            var inconsistencyState = inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
            filterRecommendedInstances(inconsistencyState);
        }
    }

    /**
     * Filter RecommendedInstances based on various heuristics. First, filter unlikely ones (low probability).
     */
    private void filterRecommendedInstances(IInconsistencyState inconsistencyState) {
        var filteredRecommendedInstances = Lists.mutable.<IRecommendedInstance> empty();
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

        return highestTypeProbability > thresholdNameAndTypeProbability && highestNameProbability > thresholdNameAndTypeProbability
                || highestTypeProbability > thresholdNameOrTypeProbability || highestNameProbability > thresholdNameOrTypeProbability;

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
