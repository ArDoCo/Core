/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.DependencyType;
import io.github.ardoco.textproviderjson.PosTag;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;

class WordImpl implements Word {

    private final Text parent;
    private final int index;
    private Word preWord;
    private Word nextWord;

    private final int sentenceNo;
    private Phrase phrase;
    private final String text;
    private final PosTag posTag;

    public WordImpl(Text parent, int index, int sentenceNo, String text, PosTag posTag) {
        this.parent = parent;
        this.index = index;
        this.preWord = null;
        this.nextWord = null;
        this.sentenceNo = sentenceNo;
        this.phrase = null;
        this.text = text;
        this.posTag = posTag;
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
        int preWordIndex = index - 1;
        if (preWord == null && preWordIndex > 0) {
            preWord = parent.words().get(preWordIndex);
        }
        return preWord;
    }

    @Override
    public Word getNextWord() {
        int nextWordIndex = index + 1;
        if (nextWord == null && nextWordIndex < parent.getLength()) {
            nextWord = parent.words().get(nextWordIndex);
        }
        return nextWord;
    }

    @Override
    public int getPosition() {
        return index;
    }

    @Override
    public String getLemma() {
        return null;
    }

    @Override
    public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyType dependencyTag) {
        return null;
    }

    @Override
    public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyType dependencyTag) {
        return null;
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

}
