/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class TextImpl implements Text {
    private ImmutableList<Sentence> sentences;

    private ImmutableList<Word> words;

    public TextImpl() {

    }

    public TextImpl(ImmutableList<Sentence> sentences) {
        this.sentences = sentences;
    }

    public void setSentences(ImmutableList<Sentence> sentences) {
        this.sentences = sentences;
    }

    @Override
    public int getLength() {
        // todo
        return 0;
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
        // TODO
        return Lists.immutable.empty();
    }
}
