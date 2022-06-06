/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.baseline;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistency;

public record SimpleMissingModelInstanceInconsistency(int sentenceNo) implements IInconsistency {

    @Override
    public String getReason() {
        return "MissingModelInstanceInconsistency in sentence " + sentenceNo;
    }

    @Override
    public String getType() {
        return "SimpleMissingModelInstance";
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        MutableSet<String[]> entries = Sets.mutable.empty();

        var sentenceNoString = "" + sentenceNo;
        String[] entry = { getType(), sentenceNoString, "_", Integer.toString(sentenceNo), Double.toString(0.1337) };
        entries.add(entry);

        return entries.toImmutable();
    }

    @Override
    public IInconsistency createCopy() {
        return new SimpleMissingModelInstanceInconsistency(sentenceNo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SimpleMissingModelInstanceInconsistency))
            return false;

        SimpleMissingModelInstanceInconsistency that = (SimpleMissingModelInstanceInconsistency) o;

        return sentenceNo == that.sentenceNo;
    }

    @Override
    public int hashCode() {
        return sentenceNo;
    }

    @Override
    public String toString() {
        return "SimpelMissingModelInstanceInconsistency{" + "sentenceNo=" + sentenceNo + '}';
    }
}
