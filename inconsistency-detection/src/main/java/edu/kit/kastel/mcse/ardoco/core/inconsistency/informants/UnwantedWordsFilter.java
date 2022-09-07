package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;

// TODO this is a prototype. It should be improved, e.g, using following ideas:
// - use ComputerScienceWords
// - improve handling: instead of filtering them completely, reduce probability? Idea: we don't want to remove super high probability RIs
// - maybe consider phrases
// - maybe check if the word is a named entity or starts with capital letter in the middle of a sentence (sign of being a named entity)
public class UnwantedWordsFilter extends Filter {

    private final ImmutableList<String> unwantedWords = Lists.immutable.with("meta", "log", "browser", "task", "operation", "case", "instance", "log",
            "script");

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
            // apply heuristics
            if (referenceContainsUnwantedWord(nounMapping) || referenceContainsPluralWord(nounMapping) || referenceContainsOnlyNumbers(nounMapping))
                return true;
        }
        return false;
    }

    private boolean referenceContainsUnwantedWord(NounMapping nounMapping) {
        var reference = nounMapping.getReference().toLowerCase();
        if (unwantedWords.contains(reference)) {
            return true;
        }
        return false;
    }

    private static boolean referenceContainsPluralWord(NounMapping nounMapping) {
        for (var word : nounMapping.getReferenceWords()) {
            if (word.getPosTag().name().toLowerCase().contains("plural"))
                return true;
        }
        return false;
    }

    private boolean referenceContainsOnlyNumbers(NounMapping nounMapping) {
        var reference = nounMapping.getReference().toLowerCase();
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(reference);
        return matcher.matches();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // nothing
    }
}
