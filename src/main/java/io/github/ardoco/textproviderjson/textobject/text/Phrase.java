/* Licensed under MIT 2022. */
package io.github.ardoco.textproviderjson.textobject.text;

import io.github.ardoco.textproviderjson.PhraseType;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

public interface Phrase {
    int getSentenceNo();

    String getText();

    PhraseType getPhraseType();

    ImmutableList<Word> getContainedWords();

    ImmutableList<Phrase> getSubPhrases();

    boolean isSuperPhraseOf(Phrase other);

    boolean isSubPhraseOf(Phrase other);

    ImmutableMap<Word, Integer> getPhraseVector();
}
