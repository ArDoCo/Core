package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

public final class BabelNetSynsetId {

    private final String synsetId;

    public BabelNetSynsetId(String synsetId) {
        this.synsetId = synsetId;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BabelNetSynsetId))
            return false;

        BabelNetSynsetId that = (BabelNetSynsetId) o;

        return synsetId.equals(that.synsetId);
    }

    @Override public int hashCode() {
        return synsetId.hashCode();
    }

    @Override public String toString() {
        return synsetId;
    }

}
