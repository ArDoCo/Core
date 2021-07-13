package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Weigelt
 */
public class MainHypothesisToken extends AbstractHypothesisToken {

    private List<AlternativeHypothesisToken> alternatives;
    int hash;

    public MainHypothesisToken(String word, int position, double confidence, HypothesisTokenType type, double startTime, double endTime,
            List<AlternativeHypothesisToken> alternatives) {
        super(word, position, confidence, type, startTime, endTime);
        this.alternatives = alternatives;
    }

    public MainHypothesisToken(String word, int position, double confidence, HypothesisTokenType type, double startTime, double endTime) {
        this(word, position, confidence, type, startTime, endTime, new ArrayList<>());
    }

    public MainHypothesisToken(String word, int position, double confidence, HypothesisTokenType type) {
        this(word, position, confidence, type, 0.0d, 0.0d, new ArrayList<>());
    }

    public MainHypothesisToken(String word, int position) {
        this(word, position, 1.0d, HypothesisTokenType.MISC);
    }

    /**
     * @return the alternatives
     */
    public List<AlternativeHypothesisToken> getAlternatives() {
        return alternatives;
    }

    /**
     * @param index the index
     * @return the alternative hypothesis at position index
     */
    public AlternativeHypothesisToken getAlternative(int index) {
        return alternatives.get(index);
    }

    /**
     * @param alternatives the alternative hypotheses to be set
     */
    public void setAlternatives(List<AlternativeHypothesisToken> alternatives) {
        this.alternatives = alternatives;
    }

    /**
     * @param alternative the alternative hypothesis to be set
     */
    public void addAlternative(AlternativeHypothesisToken alternative) {
        alternatives.add(alternative);
    }

    /**
     * @param alternative the alternative hypothesis to be set
     * @param index       the index to set the alternative hypothesis
     */
    public void addAlternativeAt(AlternativeHypothesisToken alternative, int index) {
        alternatives.add(index, alternative);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MainHypothesisToken) {
            final MainHypothesisToken other = (MainHypothesisToken) obj;
            if (alternatives.size() != other.getAlternatives().size()) {
                return false;
            }
            for (var i = 0; i < alternatives.size(); i++) {
                if (!alternatives.get(i).equals(other.getAlternatives().get(i))) {
                    return false;
                }
            }
            return super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        } else {
            if (alternatives.isEmpty()) {
                hash = super.hashCode();
            } else {
                for (AlternativeHypothesisToken alternative : alternatives) {
                    hash = 31 * hash + alternative.hashCode();
                }
            }
            return hash;
        }
    }
}
