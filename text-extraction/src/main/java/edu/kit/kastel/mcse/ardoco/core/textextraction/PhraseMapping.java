package edu.kit.kastel.mcse.ardoco.core.textextraction;

import static edu.kit.kastel.informalin.framework.common.AggregationFunctions.AVERAGE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private final Confidence confidence;

    public PhraseMapping(IPhrase phrase, ImmutableList<INounMapping> nounMappings, IClaimant claimant, double probability) {

        Objects.requireNonNull(phrase);
        Objects.requireNonNull(claimant);

        this.phrases = Lists.mutable.with(phrase);
        this.containedNounMappings = Lists.mutable.withAll(nounMappings);
        this.confidence = new Confidence(claimant, probability, DEFAULT_AGGREGATOR);
    }

    public PhraseMapping(ImmutableList<IPhrase> phrases, ImmutableList<INounMapping> nounMappings, IClaimant claimant, double probability) {
        this(phrases.get(0), nounMappings, claimant, probability);

        for (int i = 1; i < phrases.size(); i++) {
            if (phrases.get(0).getPhraseType() != phrases.get(i).getPhraseType()) {
                throw new IllegalArgumentException("All phrases in a phrase mapping should have the same phrase type!");
            }
            this.addPhase(phrases.get(i));
        }
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
