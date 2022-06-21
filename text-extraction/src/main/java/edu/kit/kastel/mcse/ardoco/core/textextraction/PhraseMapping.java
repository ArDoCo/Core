/* Licensed under MIT 2022. */
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

    private MutableList<INounMapping> containedNounMappings;
    private static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;
    private static final double DEFAULT_MAX_VECTOR_DISTANCE = 0.5;
    private Confidence confidence;

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
            this.addPhrase(phrases.get(i));
        }
    }

    private PhraseMapping(ImmutableList<IPhrase> phrases, ImmutableList<INounMapping> nounMappings, Confidence confidence) {
        this.phrases = phrases.toList();
        this.containedNounMappings = nounMappings.toList();
        this.confidence = confidence;

    }

    @Override
    public void addNounMapping(INounMapping nounMapping, IPhrase phrase) {

        if (this.getPhraseType() != phrase.getPhraseType()) {
            throw new IllegalArgumentException("The phrase type inside a phrase mapping has to be the same!");
        }

        if (!this.containedNounMappings.contains(nounMapping)) {
            this.containedNounMappings.add(nounMapping);
        }
        this.addPhrase(phrase);
    }

    @Override
    public void addPhrases(ImmutableList<IPhrase> phrases) {
        for (IPhrase phrase : phrases) {
            if (!this.phrases.contains(phrase)) {
                this.phrases.add(phrase);
            }
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
    public void addPhrase(IPhrase phrase) {
        if (!phrase.getPhraseType().equals(this.getPhraseType())) {
            throw new IllegalArgumentException("added a different phrase type for mapping");
        }
        phrases.add(phrase);
    }

    @Override
    public double getProbability() {
        return this.confidence.getConfidence();
    }

    @Override
    public Confidence getConfidence() {
        return this.confidence;
    }

    @Override
    public void removeNounMapping(INounMapping nounMapping) {
        this.containedNounMappings.remove(nounMapping);
    }

    @Override
    public IPhraseMapping removePhrase(IPhrase phrase) {

        // select all noun mappings that contain the phrase
        MutableList<INounMapping> nounMappingsToChange = containedNounMappings.select(nm -> nm.getPhrases().anySatisfy(p -> p.equals(phrase)));
        MutableList<INounMapping> nounMappingsToDelete = Lists.mutable.empty();
        MutableList<INounMapping> removedNounMappings = Lists.mutable.empty();

        for (INounMapping nounMapping : nounMappingsToChange) {

            INounMapping newNounMappingWithRemovedPhrase = nounMapping.splitByPhrase(phrase);

            removedNounMappings.add(newNounMappingWithRemovedPhrase);

            if (nounMapping.getWords().isEmpty()) {
                nounMappingsToDelete.add(nounMapping);
            }
        }

        phrases.remove(phrase);
        if (phrases.isEmpty()) {
            containedNounMappings = Lists.mutable.empty();
        }

        // return PhraseMapping out of removed phrases
        return new PhraseMapping(removedNounMappings.flatCollect(nm -> nm.getPhrases()).toImmutable(), removedNounMappings.toImmutable(), this.getConfidence());

    }

    public IPhraseMapping splitByPhrase(IPhrase phrase) {

        if (this.phrases.size() == 1) {
            return this;
        }

        MutableList<INounMapping> stayingNounMappings = Lists.mutable.empty();
        MutableList<INounMapping> nounMappingsForNewPM = Lists.mutable.empty();

        for (INounMapping oldNounMapping : containedNounMappings) {
            if (oldNounMapping.getPhrases().contains(phrase)) {

                INounMapping newNounMapping = oldNounMapping.splitByPhrase(phrase);
                nounMappingsForNewPM.add(newNounMapping);
                if (!oldNounMapping.getPhrases().isEmpty()) {
                    stayingNounMappings.add(oldNounMapping);
                }
            } else {
                stayingNounMappings.add(oldNounMapping);
            }
        }

        IPhraseMapping newPhraseMapping = new PhraseMapping(Lists.immutable.with(phrase), nounMappingsForNewPM.toImmutable(), confidence.createCopy());
        this.containedNounMappings = stayingNounMappings;

        return newPhraseMapping;
    }

    @Override
    public PhraseType getPhraseType() {
        if (phrases.isEmpty()) {
            throw new IllegalStateException("A phrase mapping should always contain some phrases!");
        }
        return phrases.get(0).getPhraseType();
    }

    @Override
    public Map<IWord, Integer> getPhraseVector() {

        MutableList<IWord> words = Lists.mutable.empty();

        for (IPhrase phrase : phrases) {
            words.addAllIterable(phrase.getContainedWords());
        }

        Map<IWord, Integer> phraseVector = new HashMap<>();
        var grouped = words.groupBy(IWord::getText).toMap();
        grouped.forEach((key, value) -> phraseVector.put(value.getAny(), value.size()));
        // TODO: Thing about norm

        return phraseVector;
    }

    @Override
    public IPhraseMapping merge(IPhraseMapping phraseMapping, Map<INounMapping, INounMapping> nounMappingReplacement) {

        if (phraseMapping.getPhraseType() != this.getPhraseType()) {
            throw new IllegalArgumentException("The phrase types inside a phrase mapping should be the same!");
        }

        mergePhrases(phraseMapping.getPhrases());

        for (INounMapping oldNounMapping : nounMappingReplacement.keySet()) {
            if (oldNounMapping != null) {
                assert (this.containedNounMappings.contains(oldNounMapping));
                this.containedNounMappings.remove(oldNounMapping);
            }

            this.containedNounMappings.add(nounMappingReplacement.get(oldNounMapping));
        }

        this.confidence = Confidence.merge(this.getConfidence(), phraseMapping.getConfidence(), DEFAULT_AGGREGATOR, AggregationFunctions.MAX);
        return this;
    }

    @Override
    public IPhraseMapping mergeAndAddNounMappings(IPhraseMapping phraseMapping, ImmutableList<INounMapping> nounMappings) {
        if (phraseMapping.getPhraseType() != this.getPhraseType()) {
            throw new IllegalArgumentException("The phrase types inside a phrase mapping should be the same!");
        }

        mergePhrases(phraseMapping.getPhrases());
        for (INounMapping nounMapping : nounMappings) {
            // TODO: Contains doesn not work here! NounMaping Should be extended!
            if (containedNounMappings.contains(nounMapping)) {
                // throw new IllegalArgumentException("This noun mapping is already contained by this phrase mapping");
            } else {
                containedNounMappings.add(nounMapping);
            }
        }

        this.confidence = Confidence.merge(this.getConfidence(), phraseMapping.getConfidence(), DEFAULT_AGGREGATOR, AggregationFunctions.MAX);
        return this;

    }

    @Override
    public boolean containsExactNounMapping(INounMapping nm) {

        return containedNounMappings.anySatisfy(cnm -> cnm.containsSameWordsAs(nm));
    }

    private void mergePhrases(ImmutableList<IPhrase> phrases) {
        this.phrases.addAllIterable(phrases.select(p -> !this.getPhrases().contains(p)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPhraseType(), getNounMappings(), getPhraseVector());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (IPhraseMapping) obj;
        return Objects.equals(getPhraseType(), other.getPhraseType()) && Objects.equals(getNounMappings(), other.getNounMappings())
                && Objects.equals(getPhraseVector(), other.getPhraseVector());
    }

    @Override
    public IPhraseMapping createCopy() {
        var pm = new PhraseMapping(phrases.toImmutable(), getNounMappings(), getConfidence().createCopy());
        return pm;
    }
}
