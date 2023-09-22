/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * This interface defines the representation of a text.
 */
public interface Text {

    /**
     * Gets the length of the text (amount of words).
     *
     * @return the length
     */
    default int getLength() {
        return words().size();
    }

    /**
     * Gets all words of the text (ordered).
     *
     * @return the words
     */
    ImmutableList<Word> words();

    /**
     * Returns the word at the given index
     *
     * @param index the index
     * @return the word at the given index
     */
    Word getWord(int index);

    /**
     * Returns the sentences of the text, ordered by appearance.
     *
     * @return the sentences of the text, ordered by appearance.
     */
    ImmutableList<Sentence> getSentences();
}
