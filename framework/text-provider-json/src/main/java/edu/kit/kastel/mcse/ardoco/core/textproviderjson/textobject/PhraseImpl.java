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
    private final PhraseType type;
    private final ImmutableList<Phrase> childPhrases;
    private final ImmutableList<Word> nonPhraseWords;
    private ImmutableList<Word> phraseWords;
    private ImmutableList<Word> containedWords;
    private ImmutableList<Phrase> subPhrases;
    private ImmutableSortedMap<Word, Integer> phraseVector;
    private int sentenceNo = -1;
    private String text;

    public PhraseImpl(ImmutableList<Word> nonPhraseWords, PhraseType type, List<Phrase> childPhrases) {
        this.nonPhraseWords = nonPhraseWords == null ? Lists.immutable.empty() : nonPhraseWords;
        this.type = type;
        this.childPhrases = Lists.immutable.ofAll(childPhrases);
    }

    @Override
    public synchronized int getSentenceNo() {
        if (sentenceNo < 0) {
            sentenceNo = getContainedWords().get(0).getSentenceNo();
        }
        return sentenceNo;
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
    public synchronized ImmutableList<Word> getContainedWords() {
        if (containedWords == null) {
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
            containedWords = words.toImmutable();
        }

        return containedWords;
    }

    @Override
    public synchronized ImmutableList<Phrase> getSubPhrases() {
        if (subPhrases == null) {
            MutableList<Phrase> tempSubPhrases = Lists.mutable.ofAll(childPhrases);
            for (Phrase childPhrase : childPhrases) {
                tempSubPhrases.addAll(childPhrase.getSubPhrases().toList());
            }
            subPhrases = tempSubPhrases.toImmutable();
        }
        return subPhrases;
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        MutableList<Phrase> subphrases = Lists.mutable.ofAll(this.getSubPhrases());
        while (!subphrases.isEmpty()) {
            if (subphrases.contains(other)) {
                return true;
            }
            subphrases = getSubPhrasesOfPhrases(subphrases);
        }
        return false;
    }

    private static MutableList<Phrase> getSubPhrasesOfPhrases(MutableList<Phrase> subphrases) {
        MutableList<Phrase> subPhrasesOfPhrases = Lists.mutable.empty();
        for (Phrase subphrase : subphrases) {
            subPhrasesOfPhrases.addAll(subphrase.getSubPhrases().castToList());
        }
        return subPhrasesOfPhrases;
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        MutableList<Phrase> subphrases = Lists.mutable.ofAll(other.getSubPhrases());
        while (!subphrases.isEmpty()) {
            if (subphrases.contains(this)) {
                return true;
            }
            subphrases = getSubPhrasesOfPhrases(subphrases);
        }
        return false;
    }

    @Override
    public synchronized ImmutableSortedMap<Word, Integer> getPhraseVector() {
        if (this.phraseVector == null) {
            MutableSortedMap<Word, Integer> tempPhraseVector = SortedMaps.mutable.empty();
            var grouped = getContainedWords().groupBy(Word::getText).toMap();
            grouped.forEach((key, value) -> tempPhraseVector.put(value.getAny(), value.size()));
            this.phraseVector = tempPhraseVector.toImmutable();
        }
        return this.phraseVector;
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
        return Objects.equals(getContainedWords(), phrase.getContainedWords()) && Objects.equals(getText(),
                phrase.getText()) && type == phrase.type && Objects.equals(childPhrases, phrase.childPhrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContainedWords(), getText(), type, childPhrases);
    }
}
