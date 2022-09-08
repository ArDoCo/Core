/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.TextInconsistency;

public record MissingModelInstanceInconsistency(String name, int sentence, double confidence) implements TextInconsistency {

    private static final String INCONSISTENCY_TYPE_NAME = "MissingModelInstance";

    @Override
    public String getReason() {
        return String.format(Locale.US, "Text indicates that \"%s\" should be contained in the model(s) but could not be found. (confidence: %.2f)", name,
                confidence);
    }

    @Override
    public int getSentenceNumber() {
        return sentence;
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        MutableSet<String[]> entries = Sets.mutable.empty();

        var sentenceNoString = "" + sentence;
        String[] entry = { getType(), sentenceNoString, name, Integer.toString(sentence), Double.toString(confidence) };
        entries.add(entry);

        return entries.toImmutable();
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sentence, confidence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MissingModelInstanceInconsistency other)) {
            return false;
        }
        return Objects.equals(name, other.name) && sentence == other.sentence && Math.abs(confidence - other.confidence) < 1e-5;
    }

    @Override
    public String toString() {
        return "MissingModelInstanceInconsistency [name=" + name + ", sentence= " + sentence + "]";
    }

}
