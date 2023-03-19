/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.DependencyType;
import io.github.ardoco.textproviderjson.PosTag;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;
import java.util.Objects;

public class WordImpl implements Word {

    private final Text parent;
    private final int indexInText;
    private Word preWord;
    private Word nextWord;

    private final int sentenceNo;
    private Phrase phrase;
    private final String text;
    private final PosTag posTag;
    private final String lemma;

    private final List<DependencyImpl> ingoingDependencies;
    private final List<DependencyImpl> outgoingDependencies;

    public WordImpl(Text parent, int index, int sentenceNo, String text, PosTag posTag, String lemma, List<DependencyImpl> inDep, List<DependencyImpl> outDep) {
        this.parent = parent;
        this.indexInText = index;
        this.preWord = null;
        this.nextWord = null;
        this.sentenceNo = sentenceNo;
        this.phrase = null;
        this.text = text;
        this.posTag = posTag;
        this.lemma = lemma;
        this.outgoingDependencies = outDep;
        this.ingoingDependencies = inDep;
    }

    @Override
    public int getSentenceNo() {
        return sentenceNo;
    }

    @Override
    public Sentence getSentence() {
        return this.parent.getSentences().get(this.sentenceNo);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public PosTag getPosTag() {
        return this.posTag;
    }

    @Override
    public Word getPreWord() {
        int preWordIndex = indexInText - 1;
        if (preWord == null && preWordIndex > 0) {
            preWord = parent.words().get(preWordIndex);
        }
        return preWord;
    }

    @Override
    public Word getNextWord() {
        int nextWordIndex = indexInText + 1;
        if (nextWord == null && nextWordIndex < parent.getLength()) {
            nextWord = parent.words().get(nextWordIndex);
        }
        return nextWord;
    }

    @Override
    public int getPosition() {
        return this.indexInText;
    }

    @Override
    public String getLemma() {
        return this.lemma;
    }

    @Override
    public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyType dependencyTag) {
        List<DependencyImpl> dependenciesOfType = this.outgoingDependencies.stream().filter(x -> x.getDependencyType() == dependencyTag).toList();
        List<Word> words = dependenciesOfType.stream().map(x -> this.parent.words().get((int)x.getWordId())).toList();
        return Lists.immutable.ofAll(words);
    }

    @Override
    public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyType dependencyTag) {
        List<DependencyImpl> dependenciesOfType = this.ingoingDependencies.stream().filter(x -> x.getDependencyType() == dependencyTag).toList();
        List<Word> words = dependenciesOfType.stream().map(x -> this.parent.words().get((int)x.getWordId())).toList();
        return Lists.immutable.ofAll(words);
    }

    @Override
    public Phrase getPhrase() {
        if (this.phrase == null) {
            this.phrase = loadPhrase();
        }
        return this.phrase;
    }

    private Phrase loadPhrase() {
        var currentPhrase = getSentence().getPhrases().stream().filter(p -> p.getContainedWords().contains(this)).findFirst().orElseThrow();
        var subPhrases = List.of(currentPhrase);
        while (!subPhrases.isEmpty()) {
            currentPhrase = subPhrases.get(0);
            subPhrases = currentPhrase.getSubPhrases().stream().filter(p -> p.getContainedWords().contains(this)).toList();
        }
        return currentPhrase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordImpl word = (WordImpl) o;
        return indexInText == word.indexInText && sentenceNo == word.sentenceNo && Objects.equals(parent, word.parent) && Objects.equals(preWord, word.preWord) && Objects.equals(nextWord, word.nextWord) && Objects.equals(phrase, word.phrase) && Objects.equals(text, word.text) && posTag == word.posTag && Objects.equals(lemma, word.lemma) && Objects.equals(ingoingDependencies, word.ingoingDependencies) && Objects.equals(outgoingDependencies, word.outgoingDependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, indexInText, preWord, nextWord, sentenceNo, phrase, text, posTag, lemma, ingoingDependencies, outgoingDependencies);
    }
}
