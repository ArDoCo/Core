/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;

public class MissingModelInstanceInconsistency implements IInconsistency {
    private static final String INCONSISTENCY_TYPE_NAME = "MissingModelInstance";

    private static final String REASON_FORMAT_STRING = "Text indicates (confidence: %.2f) that \"%s\" should be contained in the model(s) but could not be found. Sentences: %s";

    private IRecommendedInstance textualInstance;

    public MissingModelInstanceInconsistency(IRecommendedInstance textualInstance) {
        this.textualInstance = textualInstance;
    }

    @Override
    public String getReason() {
        var name = textualInstance.getName();
        var occurences = getOccurencesString();

        var confidence = textualInstance.getProbability();
        return String.format(Locale.US, REASON_FORMAT_STRING, confidence, name, occurences);
    }

    private String getOccurencesString() {
        SortedSet<Integer> occurences = new TreeSet<>();
        for (var nameMapping : textualInstance.getNameMappings()) {
            occurences.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }

        var occurenceJoiner = new StringJoiner(",");
        for (var sentence : occurences) {
            occurenceJoiner.add(Integer.toString(sentence));
        }
        return occurenceJoiner.toString();
    }

    @Override
    public IInconsistency createCopy() {
        return new MissingModelInstanceInconsistency(textualInstance.createCopy());
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        MutableSet<String[]> entries = Sets.mutable.empty();

        var name = textualInstance.getName();
        for (var nameMapping : textualInstance.getNameMappings()) {
            if (nameMapping.getKind() != MappingKind.TYPE) {
                for (var word : nameMapping.getWords()) {
                    var sentenceNoString = "" + (word.getSentenceNo() + 1);
                    String[] entry = { getType(), sentenceNoString, name, word.getText(), Double.toString(textualInstance.getProbability()) };
                    entries.add(entry);
                }
            }
        }

        return entries.toImmutable();
    }

    public IRecommendedInstance getTextualInstance() {
        return textualInstance;
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(textualInstance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (MissingModelInstanceInconsistency) obj;
        return Objects.equals(textualInstance, other.textualInstance);
    }

    @Override
    public String toString() {
        return "MissingModelInstanceInconsistency [textualInstance=" + textualInstance + "]";
    }

}
