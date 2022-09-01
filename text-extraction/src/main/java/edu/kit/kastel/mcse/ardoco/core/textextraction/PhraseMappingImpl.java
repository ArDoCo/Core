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

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;

public class PhraseMappingImpl implements PhraseMapping {

    /**
     * Phrases encapsulated in the mapping.
     */
    private final MutableSet<Phrase> phrases;

    public PhraseMappingImpl(ImmutableSet<Phrase> phrases) {
        this.phrases = Sets.mutable.empty();

        PhraseType type = phrases.getAny().getPhraseType();
        for (Phrase phrase : phrases) {
            /*
             * if (phrase.getPhraseType() != type) { throw new
             * IllegalArgumentException("Multiple Types of Phrase mappings"); }
             */
            this.phrases.add(phrase);
        }
    }

    @Override
    public void addPhrases(ImmutableList<Phrase> phrases) {
        for (Phrase phrase : phrases) {
            if (!this.phrases.contains(phrase)) {
                this.phrases.add(phrase);
            }
        }
    }

    @Override
    public ImmutableList<NounMapping> getNounMappings(TextState textState) {
        return textState.getNounMappings().select(nm -> Sets.mutable.withAll(nm.getWords()).equals(phrases.flatCollect(Phrase::getContainedWords)));
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        return phrases.toImmutableList();
    }

    @Override
    public void addPhrase(Phrase phrase) {
        if (!phrase.getPhraseType().equals(this.getPhraseType())) {
            throw new IllegalArgumentException("When added to a phrase mapping, phrases should have the same phrase type as the phrases of the mapping");
        }
        phrases.add(phrase);
    }

    @Override
    public void removePhrase(Phrase phrase) {

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
    public Map<Word, Integer> getPhraseVector() {

        MutableList<Word> words = Lists.mutable.empty();

        for (Phrase phrase : phrases) {
            words.addAllIterable(phrase.getContainedWords());
        }

        Map<Word, Integer> phraseVector = new HashMap<>();
        var grouped = words.groupBy(Word::getText).toMap();
        grouped.forEach((key, value) -> phraseVector.put(value.getAny(), value.size()));
        // TODO: Think about norm

        return phraseVector;
    }

    public boolean isAlmostEqual(TextState textState, PhraseMapping other) {
        return Objects.equals(getPhraseType(), other.getPhraseType()) && Objects.equals(getNounMappings(textState), other.getNounMappings(textState)) && Objects
                .equals(getPhraseVector(), other.getPhraseVector());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PhraseMappingImpl that = (PhraseMappingImpl) o;
        return Objects.equals(phrases, that.phrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phrases);
    }

    @Override
    public PhraseMapping createCopy() {
        return new PhraseMappingImpl(phrases.toImmutable());
    }

    @Override
    public String toString() {
        return "PhraseMapping{" + "phrases=" + String.join(",", phrases.collect(Phrase::toString)) + '}';
    }
}
