/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.filters;

import java.util.Comparator;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractFilter;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * Filters {@link IRecommendedInstance}s that occur only once in the text, thus are unlikely to be important entities.
 *
 * @author Jan Keim
 *
 */
public class OccasionFilter extends AbstractFilter {

    @Configurable
    private int expectedAppearances = 2;

    public OccasionFilter(DataRepository dataRepository) {
        super("OccasionFilter", dataRepository);
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

    private void filterRecommendedInstances(IInconsistencyState inconsistencyState) {
        var filteredRecommendedInstances = Lists.mutable.<IRecommendedInstance> empty();
        var recommendedInstances = inconsistencyState.getRecommendedInstances();

        for (var recommendedInstance : recommendedInstances) {
            if (recommendedInstanceHasMultipleOccasions(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }
        inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    private boolean recommendedInstanceHasMultipleOccasions(IRecommendedInstance recommendedInstance) {
        var counterDifferentTextPositions = countDifferentTextPositions(recommendedInstance);

        return counterDifferentTextPositions >= expectedAppearances;
    }

    private int countDifferentTextPositions(IRecommendedInstance recommendedInstance) {
        // phrases exist of words that are directly following each other. Therefore, we look if words appear not
        // directly after each other to count different text positions
        var counter = 0;
        var words = recommendedInstance.getNameMappings().flatCollect(INounMapping::getWords).toSortedSet(Comparator.comparingInt(IWord::getPosition));
        var lastPosition = -1337;
        for (var word : words) {
            var position = word.getPosition();
            if (position > lastPosition + 1) {
                counter++;
            }
            lastPosition = position;
        }
        return counter;
    }

    // Alternative to counting different text positions
    @SuppressWarnings("unused")
    private int countSurfaceForms(IRecommendedInstance recommendedInstance) {
        var counter = 0;
        var nameMappings = recommendedInstance.getNameMappings();
        for (var nameMapping : nameMappings) {
            counter += nameMapping.getSurfaceForms().size();
        }
        return counter;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }

}
