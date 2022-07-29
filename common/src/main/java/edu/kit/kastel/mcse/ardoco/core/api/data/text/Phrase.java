/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

import java.util.List;

public interface Phrase {
    int getSentenceNo();

    Sentence getSentence();

    String getText();

    PhraseType getPhraseType();

    List<Word> getContainedWords();

    List<Phrase> getSubPhrases();

    boolean isSuperPhraseOf(Phrase other);

    boolean isSubPhraseOf(Phrase other);
}
