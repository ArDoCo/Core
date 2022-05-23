/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

import java.util.List;

public interface IPhrase {
    int getSentenceNo();

    ISentence getSentence();

    String getText();

    PhraseType getPhraseType();

    List<IWord> getContainedWords();

    List<IPhrase> getSubPhrases();

    boolean isSuperPhraseOf(IPhrase other);

    boolean isSubPhraseOf(IPhrase other);
}
