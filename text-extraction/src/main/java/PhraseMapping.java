import static edu.kit.kastel.informalin.framework.common.AggregationFunctions.AVERAGE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;

public class PhraseMapping implements IPhraseMapping {

    /**
     * Phrases encapsulated in the mapping.
     */
    private final MutableList<IPhrase> phrases;

    private static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;
    private final MutableList<INounMapping> containedNounMappings;
    private Confidence confidence;

    public PhraseMapping(ImmutableList<IPhrase> phrases, ImmutableList<INounMapping> nounMappings, IClaimant claimant, double probability) {

        if (phrases.size() < 2) {
            throw new IllegalArgumentException("There should be at least one phrase to generate a phrase mapping!");
        }
        for (IPhrase phrase : phrases) {
            if (phrases.get(0).getPhraseType() != phrase.getPhraseType()) {
                throw new IllegalArgumentException("All phrases in a phrase mapping should have the same phrase type!");
            }
        }
        this.phrases = Lists.mutable.withAll(phrases);
        this.containedNounMappings = Lists.mutable.withAll(nounMappings);
        this.confidence = new Confidence(claimant, probability, DEFAULT_AGGREGATOR);
    }

    @Override
    public ImmutableList<INounMapping> getNounMappings() {
        return this.containedNounMappings.toImmutable();
    }

    @Override
    public ImmutableList<IPhrase> getPhrases() {
        return phrases.toImmutable();
    }

    @Override
    public void addPhase(IPhrase phrase) {
        phrases.add(phrase);
    }

    @Override
    public double getProbability() {
        return this.confidence.getConfidence();
    }

    @Override
    public PhraseType getPhraseType() {
        return phrases.get(0).getPhraseType();
    }

    @Override
    public Map<IWord, Double> getPhraseVector() {

        MutableList<IWord> words = phrases.flatCollect(IPhrase::getContainedWords);

        Map<IWord, Double> phraseVector = new HashMap<>();

        for (IWord word : words) {
            if (phrases.contains(word)) {
                continue;
            }
            phraseVector.put(word, (words.count(w -> w == word) / (double) words.size()));

        }
        return phraseVector;
    }
}
