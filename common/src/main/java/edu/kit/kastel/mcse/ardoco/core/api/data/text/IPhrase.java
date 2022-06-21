/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

public interface IPhrase {
    int getSentenceNo();

    ISentence getSentence();

    String getText();

    PhraseType getPhraseType();

    ImmutableList<IWord> getContainedWords();

    ImmutableList<IPhrase> getSubPhrases();

    boolean isSuperPhraseOf(IPhrase other);

    boolean isSubPhraseOf(IPhrase other);

    Map<IWord, Integer> getPhraseVector();
}
