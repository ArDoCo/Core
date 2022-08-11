/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

public interface Phrase {
    int getSentenceNo();

    String getText();

    PhraseType getPhraseType();

    ImmutableList<Word> getContainedWords();

    ImmutableList<Phrase> getSubPhrases();

    boolean isSuperPhraseOf(Phrase other);

    boolean isSubPhraseOf(Phrase other);

    Map<Word, Integer> getPhraseVector();
}
