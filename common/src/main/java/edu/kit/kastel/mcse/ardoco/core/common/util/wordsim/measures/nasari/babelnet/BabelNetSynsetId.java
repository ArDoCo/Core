/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import java.util.Objects;

/**
 * Represents the unique identifier of a BabelNet synset.
 */
public final class BabelNetSynsetId {

    private final String synsetId;

    /**
     * Constructs a new {@link BabelNetSynsetId} using the given string.
     * 
     * @param synsetId the synset id as a string
     */
    public BabelNetSynsetId(String synsetId) {
        this.synsetId = Objects.requireNonNull(synsetId);

        if (synsetId.isEmpty()) {
            throw new IllegalArgumentException("synsetId cannot be empty.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BabelNetSynsetId that))
            return false;

        return synsetId.equals(that.synsetId);
    }

    @Override
    public int hashCode() {
        return synsetId.hashCode();
    }

    @Override
    public String toString() {
        return synsetId;
    }

}
