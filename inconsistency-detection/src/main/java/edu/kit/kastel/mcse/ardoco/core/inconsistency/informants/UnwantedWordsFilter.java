package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;

public class UnwantedWordsFilter extends Filter {

    private final ImmutableList<String> filterWords = Lists.immutable.with("meta", "log", "server", "browser", "task", "operation", "case", "instance");

    public UnwantedWordsFilter(DataRepository dataRepository) {
        super(UnwantedWordsFilter.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void filterRecommendedInstances(InconsistencyState inconsistencyState) {
        var filteredRecommendedInstances = Lists.mutable.<RecommendedInstance>empty();
        var recommendedInstances = inconsistencyState.getRecommendedInstances();

        for (var recommendedInstance : recommendedInstances) {
            boolean shallBeFiltered = checkRecommendedInstance(recommendedInstance);
            if (!shallBeFiltered)
                filteredRecommendedInstances.add(recommendedInstance);
        }

        inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    private boolean checkRecommendedInstance(RecommendedInstance recommendedInstance) {
        for (var nounMapping : recommendedInstance.getNameMappings()) {

            // filter unwanted words from word list
            var reference = nounMapping.getReference().toLowerCase();
            if (filterWords.contains(reference)) {
                return true;
            }

            // filter plural words (heuristic)
            for (var word : nounMapping.getReferenceWords()) {
                if (word.getPosTag().name().toLowerCase().contains("plural"))
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // nothing
    }
}
