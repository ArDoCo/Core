/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * Represents a sentence within the document.
 *
 * @author Jan Keim
 *
 */
public interface ISentence {

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
    ImmutableList<IWord> getWords();

    /**
     * Returns the text of this sentence
     *
     * @return the text of this sentence
     */
    String getText();

}
