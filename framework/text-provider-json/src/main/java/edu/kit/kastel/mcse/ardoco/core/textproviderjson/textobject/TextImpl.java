/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public class TextImpl implements Text {
    private ImmutableList<Sentence> sentences;

    private ImmutableList<Word> words;

    public TextImpl() {
        sentences = Lists.immutable.empty();
        words = Lists.immutable.empty();
    }

    public void setSentences(ImmutableList<Sentence> sentences) {
        this.sentences = sentences;
    }

    @Override
    public int getLength() {
        int length = 0;
        for (Sentence sentence : sentences) {
            length += sentence.getWords().size();
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
        for (Sentence sentence : sentences) {
            collectedWords.addAll(sentence.getWords().castToCollection());
        }
        return collectedWords.toImmutable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TextImpl text))
            return false;
        return Objects.equals(sentences, text.sentences) && Objects.equals(words, text.words);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentences, words);
    }
}
