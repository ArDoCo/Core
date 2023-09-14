/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public class PhraseImpl implements Phrase {
    private static final String PUNCTUATION_WITH_SPACE = "\\s+([.,;:?!])";
    private static final String BRACKETS_WITH_SPACE = "\\s+([()\\[\\]{}<>])";
    private final ImmutableList<Word> nonPhraseWords;
    private ImmutableList<Word> phraseWords;

    private String text;

    private final PhraseType type;

    private final List<Phrase> childPhrases;

    public PhraseImpl(ImmutableList<Word> nonPhraseWords, PhraseType type, List<Phrase> childPhrases) {
        this.nonPhraseWords = nonPhraseWords;
        this.type = type;
        this.childPhrases = childPhrases;
    }

    @Override
    public int getSentenceNo() {
        return getContainedWords().get(0).getSentenceNo();
    }

    @Override
    public String getText() {
        if (this.text == null) {
            MutableList<Word> wordList = getContainedWords().toList();
            wordList.sort(Comparator.comparingInt(Word::getPosition));
            List<String> wordText = wordList.collect(Word::getText);
            // Join string with spaces but remove spaces before punctuation and brackets
            this.text = String.join(" ", wordText).replaceAll(PUNCTUATION_WITH_SPACE, "$1").replaceAll(BRACKETS_WITH_SPACE, "$1");
        }
        return this.text;
    }

    @Override
    public PhraseType getPhraseType() {
        return this.type;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        if (phraseWords == null) {
            List<Word> collectedWords = new ArrayList<>();
            for (Phrase subphrase : childPhrases) {
                collectedWords.addAll(subphrase.getContainedWords().castToList());
            }
            this.phraseWords = Lists.immutable.ofAll(collectedWords);
        }

        MutableList<Word> words = Lists.mutable.ofAll(nonPhraseWords);
        words.addAllIterable(phraseWords);
        words.sortThis(Comparator.comparingInt(Word::getPosition));
        return words.toImmutable();
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
    public ImmutableSortedMap<Word, Integer> getPhraseVector() {
        MutableSortedMap<Word, Integer> phraseVector = SortedMaps.mutable.empty();

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
        return Objects.equals(getContainedWords(), phrase.getContainedWords()) && Objects.equals(getText(), phrase.getText()) && type == phrase.type && Objects
                .equals(childPhrases, phrase.childPhrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContainedWords(), getText(), type, childPhrases);
    }
}
