/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject;

import io.github.ardoco.textproviderjson.PhraseType;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public class PhraseImpl implements Phrase {
    private final ImmutableList<Word> words;

    private final Sentence parent;

    private String text;

    private PhraseType type;

    public PhraseImpl(ImmutableList<Word> words, Sentence parent, String text, PhraseType type) {
        this.words = words;
        this.parent = parent;
        this.text = text;
        this.type = type;
    }

    @Override
    public int getSentenceNo() {
        return words.get(0).getSentenceNo();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public PhraseType getPhraseType() {
        return this.type;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        return words;
    }

    @Override
    public ImmutableList<Phrase> getSubPhrases() {
        // todo
        return null;
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        // todo
        return false;
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        // todo
        return false;
    }

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
}
