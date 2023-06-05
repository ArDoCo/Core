/* Licensed under MIT 2022-2023. */
package io.github.ardoco.textproviderjson.textobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

public class PhraseImpl implements Phrase {
    private ImmutableList<Word> words;

    private String text = "";

    private final PhraseType type;

    private final List<Phrase> childPhrases;

    public PhraseImpl(ImmutableList<Word> words, PhraseType type, List<Phrase> childPhrases) {
        this.words = words;
        this.type = type;
        this.childPhrases = childPhrases;
    }

    @Override
    public int getSentenceNo() {
        return words.get(0).getSentenceNo();
    }

    @Override
    public String getText() {
        if (this.text == null) {
            List<Word> wordList = getContainedWords().castToList();
            wordList.sort((word1, word2) -> word1.getPosition() - word2.getPosition());
            List<String> wordText = wordList.stream().map(Word::getText).toList();
            this.text = String.join(" ", wordText);
        }
        return this.text;
    }

    @Override
    public PhraseType getPhraseType() {
        return this.type;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        if (words == null) {
            List<Word> collectedWords = new ArrayList<>();
            for (Phrase subphrase : childPhrases) {
                collectedWords.addAll(subphrase.getContainedWords().castToList());
            }
            this.words = Lists.immutable.ofAll(collectedWords);
        }
        return words;
    }

    @Override
    public ImmutableList<Phrase> getSubPhrases() {
        List<Phrase> subPhrases = new ArrayList<>(childPhrases);
        for (Phrase childPhrase : childPhrases) {
            subPhrases.addAll(childPhrase.getSubPhrases().toList());
        }
        return Lists.immutable.ofAll(subPhrases);
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        List<Phrase> subphrases = this.childPhrases;
        while (!subphrases.isEmpty()) {
            if (subphrases.contains(other)) {
                return true;
            }
            List<Phrase> newSubphrases = new ArrayList<>();
            for (Phrase subphrase : subphrases) {
                newSubphrases.addAll(subphrase.getSubPhrases().castToList());
            }
            subphrases = newSubphrases;
        }
        return false;
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        List<Phrase> subphrases = other.getSubPhrases().castToList();
        while (!subphrases.isEmpty()) {
            if (subphrases.contains(this)) {
                return true;
            }
            List<Phrase> newSubphrases = new ArrayList<>();
            for (Phrase subphrase : subphrases) {
                newSubphrases.addAll(subphrase.getSubPhrases().castToList());
            }
            subphrases = newSubphrases;
        }
        return false;
    }

    @Override
    public ImmutableMap<Word, Integer> getPhraseVector() {
        MutableMap<Word, Integer> phraseVector = Maps.mutable.empty();

        var grouped = getContainedWords().groupBy(Word::getText).toMap();
        grouped.forEach((key, value) -> phraseVector.put(value.getAny(), value.size()));

        return phraseVector.toImmutable();
    }

    @Override
    public String toString() {
        return "Phrase{" + "text='" + getText() + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhraseImpl phrase))
            return false;
        return Objects.equals(words, phrase.words) && Objects.equals(text, phrase.text) && type == phrase.type && Objects.equals(childPhrases,
                phrase.childPhrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(words, text, type, childPhrases);
    }
}
