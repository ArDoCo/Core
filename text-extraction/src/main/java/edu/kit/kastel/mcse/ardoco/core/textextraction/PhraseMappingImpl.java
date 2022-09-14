/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.*;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMappingChangeListener;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;

public final class PhraseMappingImpl implements PhraseMapping {

    /**
     * Phrases encapsulated in the mapping.
     */
    private final Set<Phrase> phrases;

    private final Set<PhraseMappingChangeListener> changeListeners = Collections.newSetFromMap(new IdentityHashMap<>());

    public PhraseMappingImpl(Collection<Phrase> phrases) {
        this.phrases = Collections.newSetFromMap(new IdentityHashMap<>());
        this.phrases.addAll(phrases);
    }

    @Override
    public ImmutableList<NounMapping> getNounMappings(TextState textState) {
        var allContainedWords = Sets.mutable.fromStream(phrases.stream().flatMap(phrase -> phrase.getContainedWords().stream()));
        return textState.getNounMappings().select(nm -> Sets.mutable.withAll(nm.getWords()).equals(allContainedWords));
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        return Lists.immutable.withAll(phrases);
    }

    @Override
    public void removePhrase(Phrase phrase) {
        // TODO @Phi: That's not done by remove. Remove deletes exactly one .. pls check comment below.
        // select all noun mappings that contain the phrase
        phrases.remove(phrase);
        assert !phrases.isEmpty(); // PhraseMappings cannot be empty!
    }

    @Override
    public PhraseType getPhraseType() {
        if (phrases.isEmpty()) {
            throw new IllegalStateException("A phrase mapping should always contain some phrases!");
        }
        return phrases.iterator().next().getPhraseType();
    }

    @Override
    public ImmutableMap<Word, Integer> getPhraseVector() {
        MutableList<Word> words = Lists.mutable.empty();

        for (Phrase phrase : phrases) {
            words.addAllIterable(phrase.getContainedWords());
        }

        MutableMap<Word, Integer> phraseVector = Maps.mutable.empty();
        var grouped = words.groupBy(Word::getText).toMap();
        grouped.forEach((key, value) -> phraseVector.put(value.getAny(), value.size()));
        // TODO: Think about norm

        return phraseVector.toImmutable();
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
    public String toString() {
        return "PhraseMapping{" + "phrases=" + phrases + '}';
    }

    @Override
    public void registerChangeListener(PhraseMappingChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public void onDelete(PhraseMapping replacement) {
        changeListeners.forEach(l -> l.onDelete(this, replacement));
    }
}
