package edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures;

import java.util.Locale;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;

public class MissingModelInstanceInconsistency implements IInconsistency {

    private static final String REASON_FORMAT_STRING = "Text indicates (confidence: %.2f) that \"%s\" should be contained in the model(s) but could not be found. Sentences: %s";

    private IRecommendedInstance textualInstance;

    public MissingModelInstanceInconsistency(IRecommendedInstance textualInstance) {
        this.textualInstance = textualInstance;
    }

    @Override
    public String getReason() {
        var name = textualInstance.getName();
        SortedSet<Integer> occurences = new TreeSet<>();
        for (var nameMapping : textualInstance.getNameMappings()) {
            occurences.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }

        var occurenceJoiner = new StringJoiner(",");
        for (var sentence : occurences) {
            occurenceJoiner.add(Integer.toString(sentence));
        }

        var confidence = textualInstance.getProbability();
        return String.format(Locale.US, REASON_FORMAT_STRING, confidence, name, occurenceJoiner.toString());
    }

    @Override
    public IInconsistency createCopy() {
        return new MissingModelInstanceInconsistency(textualInstance.createCopy());
    }

}
