/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;

public class PhraseMapping implements IPhraseMapping {

    /**
     * Phrases encapsulated in the mapping.
     */
    private final MutableSet<IPhrase> phrases;

    public PhraseMapping(ImmutableSet<IPhrase> phrases) {
        this.phrases = Sets.mutable.empty();

        PhraseType type = phrases.getAny().getPhraseType();
        for (IPhrase phrase : phrases) {
            if (phrase.getPhraseType() != type) {
                throw new IllegalArgumentException("Multiple Types of Phrase mappings");
            }
            this.phrases.add(phrase);
        }
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
    public ImmutableList<INounMapping> getNounMappings(ITextState textState) {
        return textState.getNounMappings().select(nm -> Sets.mutable.withAll(nm.getWords()).equals(phrases.flatCollect(IPhrase::getContainedWords)));
    }

    @Override
    public ImmutableList<IPhrase> getPhrases() {
        return phrases.toImmutableList();
    }

    @Override
    public void addPhrase(IPhrase phrase) {
        if (!phrase.getPhraseType().equals(this.getPhraseType())) {
            throw new IllegalArgumentException("When added to a phrase mapping, phrases should have the same phrase type as the phrases of the mapping");
        }
        phrases.add(phrase);
    }

    @Override
    public void removePhrase(IPhrase phrase) {

        // select all noun mappings that contain the phrase
        phrases.remove(phrase);

    }

    @Override
    public PhraseType getPhraseType() {
        if (phrases.isEmpty()) {
            throw new IllegalStateException("A phrase mapping should always contain some phrases!");
        }
        return phrases.getAny().getPhraseType();
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
        // TODO: Think about norm

        return phraseVector;
    }

    @Override
    public IPhraseMapping merge(IPhraseMapping phraseMapping) {

        if (phraseMapping.getPhraseType() != this.getPhraseType()) {
            throw new IllegalArgumentException("If two phrase mappings are merged, they should have the same phrase type!");
        }
        this.phrases.addAllIterable(phrases.select(p -> !this.getPhrases().contains(p)));
        return this;
    }

    @Override
    public boolean containsExactNounMapping(ITextState textState, INounMapping nm) {

        return getNounMappings(textState).anySatisfy(cnm -> cnm.containsSameWordsAs(nm));
    }

    public boolean isAlmostEqual(ITextState textState, IPhraseMapping other) {

        return Objects.equals(getPhraseType(), other.getPhraseType()) && Objects.equals(getNounMappings(textState), other.getNounMappings(textState))
                && Objects.equals(getPhraseVector(), other.getPhraseVector());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PhraseMapping that = (PhraseMapping) o;
        return Objects.equals(phrases, that.phrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phrases);
    }

    @Override
    public IPhraseMapping createCopy() {
        return new PhraseMapping(phrases.toImmutable());
    }

    @Override
    public String toString() {
        return "PhraseMapping{" + "phrases=" + String.join(",", phrases.collect(IPhrase::toString)) + '}';
    }
}
