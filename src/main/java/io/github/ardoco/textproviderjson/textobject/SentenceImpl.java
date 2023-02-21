/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

class SentenceImpl implements Sentence {

    private ImmutableList<Word> words = Lists.immutable.empty();
    private ImmutableList<Phrase> phrases = Lists.immutable.empty();

    private final TextImpl parent;
    private final int sentenceNumber;

    private String text;

    public SentenceImpl(ImmutableList<Phrase> phrases, TextImpl parent, int sentenceNumber, String text) {
        this.phrases = phrases;
        this.sentenceNumber = sentenceNumber;
        this.parent = parent;
        this.text = text;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<Word> getWords() {
        if (words.isEmpty()) {
            this.words = parent.words().select(w -> w.getSentenceNo() == sentenceNumber).toImmutable();
        }
        return words;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        // todo
        return Lists.immutable.empty();
    }

}
