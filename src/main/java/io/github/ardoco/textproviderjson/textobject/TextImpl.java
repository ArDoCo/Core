/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Objects;

public class TextImpl implements Text {
    private ImmutableList<Sentence> sentences;

    private ImmutableList<Word> words;

    public TextImpl() {

    }

    public TextImpl(ImmutableList<Sentence> sentences) {
        this.sentences = sentences;
    }

    @Override
    public void setSentences(ImmutableList<Sentence> sentences) {
        this.sentences = sentences;
    }

    @Override
    public int getLength() {
        int length = 0;
        for (Sentence sentence: sentences) {
            length += sentence.getText().length();
        }
        return length;
    }

    @Override
    public ImmutableList<Word> words() {
        if (words.isEmpty()) {
            words = collectWords();
        }
        return words;
    }

    @Override
    public ImmutableList<Sentence> getSentences() {
        return sentences;
    }

    private ImmutableList<Word> collectWords() {
        MutableList<Word> collectedWords = Lists.mutable.empty();
        for (Sentence sentence: sentences) {
            collectedWords.addAll(sentence.getWords().castToCollection());
        }
        return collectedWords.toImmutable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextImpl text = (TextImpl) o;
        return Objects.equals(sentences, text.sentences) && Objects.equals(words, text.words);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentences, words);
    }
}
