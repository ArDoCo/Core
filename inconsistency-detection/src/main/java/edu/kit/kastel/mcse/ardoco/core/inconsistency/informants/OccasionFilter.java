/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import java.util.Comparator;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;

/**
 * Filters {@link RecommendedInstance}s that occur only once in the text, thus are unlikely to be important entities.
 *
 *
 */
public class OccasionFilter extends Filter {

    @Configurable
    private int expectedAppearances = 2;

    public OccasionFilter(DataRepository dataRepository) {
        super(OccasionFilter.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void filterRecommendedInstances(InconsistencyState inconsistencyState) {
        var filteredRecommendedInstances = Lists.mutable.<RecommendedInstance>empty();
        var recommendedInstances = inconsistencyState.getRecommendedInstances();

        for (var recommendedInstance : recommendedInstances) {
            if (recommendedInstanceHasMultipleOccasions(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }
        inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    private boolean recommendedInstanceHasMultipleOccasions(RecommendedInstance recommendedInstance) {
        var counterDifferentTextPositions = countDifferentTextPositions(recommendedInstance);

        return counterDifferentTextPositions >= expectedAppearances;
    }

    private int countDifferentTextPositions(RecommendedInstance recommendedInstance) {
        // phrases exist of words that are directly following each other. Therefore, we look if words appear not
        // directly after each other to count different text positions
        var counter = 0;
        var words = recommendedInstance.getNameMappings().flatCollect(NounMapping::getWords).toSortedSet(Comparator.comparingInt(Word::getPosition));
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
    private int countSurfaceForms(RecommendedInstance recommendedInstance) {
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
