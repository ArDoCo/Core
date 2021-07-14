package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token;

/**
 * @author Sebastian Weigelt
 */
public class AlternativeHypothesisToken extends AbstractHypothesisToken {

    private int hash;

    public AlternativeHypothesisToken(String word, int position, double confidence, HypothesisTokenType type, double startTime, double endTime) {
        super(word, position, confidence, type, startTime, endTime);
    }

    public AlternativeHypothesisToken(String word, int position, double confidence, HypothesisTokenType type) {
        this(word, position, confidence, type, 0.0d, 0.0d);
    }

    public AlternativeHypothesisToken(String word, int position) {
        this(word, position, 1.0d, HypothesisTokenType.MISC);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        } else {
            hash = super.hashCode();
            return hash;
        }
    }
}
