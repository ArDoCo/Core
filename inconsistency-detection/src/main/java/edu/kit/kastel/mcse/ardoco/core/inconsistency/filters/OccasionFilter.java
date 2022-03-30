/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.filters;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractFilter;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;

/**
 * @author Jan Keim
 *
 */
public class OccasionFilter extends AbstractFilter<InconsistencyAgentData> {

    public OccasionFilter() {
        // empty
    }

    @Override
    public void exec(InconsistencyAgentData data) {
        for (var model : data.getModelIds()) {
            var inconsistencyState = data.getInconsistencyState(model);
            filterRecommendedInstances(inconsistencyState);
        }

        // TODO other idea: go over each word. Find corresponding NounMappings. Find out, if there are "other" words
        // (that are thus similar).
        // However, need to find out how to then filter RecommendedInstances out.
        // Maybe: Find all NounMappings that only refer to one word (/phrase) and get the RecommendedInstances. If they
        // only have said NounMappings, remove it.
        // Something like that...
    }

    private void filterRecommendedInstances(IInconsistencyState inconsistencyState) {
        var filteredRecommendedInstances = Lists.mutable.<IRecommendedInstance> empty();
        var recommendedInstances = inconsistencyState.getRecommendedInstances();

        logger.debug("#RIs={}", recommendedInstances.size());
        for (var recommendedInstance : recommendedInstances) {
            if (recommendedInstanceHasMultipleOccasions(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }
        logger.debug("#RIs={}", filteredRecommendedInstances.size());
        inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    private boolean recommendedInstanceHasMultipleOccasions(IRecommendedInstance recommendedInstance) {
        var nameMappings = recommendedInstance.getNameMappings();

        var counter = 0;
        for (var nameMapping : nameMappings) {
            counter += nameMapping.getSurfaceForms().size();
        }
        if (counter < 2) {
            logger.info("Counter < 2: {}", recommendedInstance.getName());
        }

        return counter >= 2;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }

}
