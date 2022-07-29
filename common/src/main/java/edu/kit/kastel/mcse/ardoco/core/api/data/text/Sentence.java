/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * Represents a sentence within the document.
 *
 *
 */
public interface Sentence {

    /**
     * Returns the sentence number
     *
     * @return the sentence number
     */
    int getSentenceNumber();

    /**
     * Returns the words contained by this sentence
     *
     * @return the words contained by this sentence
     */
    ImmutableList<Word> getWords();

    /**
     * Returns the text of this sentence
     *
     * @return the text of this sentence
     */
    String getText();

    default boolean isEqualTo(Sentence other) {
        return other != null && this.getSentenceNumber() == other.getSentenceNumber() && other.getText().equals(this.getText());
    }

    ImmutableList<Phrase> getPhrases();
}
