/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Objects;

public class SentenceImpl implements Sentence {

    private final ImmutableList<Word> words;
    private ImmutableList<Phrase> phrases = Lists.immutable.empty();

    private final Text parent;
    private final int sentenceNumber;

    private final String text;

    public SentenceImpl(Text parent, int sentenceNumber, String text, ImmutableList<Word> words) {
        this.sentenceNumber = sentenceNumber;
        this.parent = parent;
        this.text = text;
        this.words = words;
    }

    @Override
    public void setPhrases(ImmutableList<Phrase> phrases) {
        this.phrases = phrases;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<Word> getWords() {
        return words;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        return this.phrases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SentenceImpl sentence = (SentenceImpl) o;
        return sentenceNumber == sentence.sentenceNumber && Objects.equals(words, sentence.words) && Objects.equals(phrases, sentence.phrases) && Objects.equals(text, sentence.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(words, phrases, sentenceNumber, text);
    }
}
