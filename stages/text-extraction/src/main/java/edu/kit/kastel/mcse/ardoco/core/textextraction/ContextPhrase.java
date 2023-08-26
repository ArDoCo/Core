package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public class ContextPhrase implements Phrase {
    private final MutableList<Word> words;
    private final Sentence sentence;

    public ContextPhrase(ImmutableList<Word> words, Sentence sentence) {
        this.words = Lists.mutable.ofAll(words);
        this.sentence = sentence;
    }

    @Override
    public int getSentenceNo() {
        return sentence.getSentenceNumber();
    }

    @Override
    public String getText() {
        return words.stream().map(Word::getText).collect(Collectors.joining(" "));
    }

    @Override
    public PhraseType getPhraseType() {
        return PhraseType.NP;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        return words.toImmutableList();
    }

    @Override
    public ImmutableList<Phrase> getSubPhrases() {
        return Lists.immutable.empty();
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        var currText = getText();
        var otherText = other.getText();
        return currText.contains(otherText) && currText.length() != otherText.length();
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        var currText = getText();
        var otherText = other.getText();
        return otherText.contains(currText) && currText.length() != otherText.length();
    }

    @Override
    public ImmutableSortedMap<Word, Integer> getPhraseVector() {
        MutableSortedMap<Word, Integer> phraseVector = SortedMaps.mutable.empty();

        var grouped = getContainedWords().groupBy(Word::getText).toMap();
        grouped.forEach((key, value) -> phraseVector.put(value.getAny(), value.size()));

        return phraseVector.toImmutable();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSentenceNo(), this.getText(), this.getPhraseType(), this.getContainedWords().get(0).getPosition());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Phrase other))
            return false;
        return this.getSentenceNo() == other.getSentenceNo() && Objects.equals(this.getText(), other.getText()) && Objects.equals(this.getPhraseType(),
                other.getPhraseType()) && this.getContainedWords().get(0).getPosition() == other.getContainedWords().get(0).getPosition();
    }

    @Override
    public String toString() {
        return "Phrase{" + "text='" + getText() + '\'' + '}';
    }
}
