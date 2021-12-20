/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * The Interface IText defines the representation of a text.
 */
public interface IText {

    /**
     * Gets the first word of the text.
     *
     * @return the first word
     */
    IWord getFirstWord();

    /**
     * Gets the length of the text (amount of words).
     *
     * @return the length
     */
    default int getLength() {
        return getWords().size();
    }

    /**
     * Gets all words of the text (ordered).
     *
     * @return the words
     */
    ImmutableList<IWord> getWords();

    /**
     * Get all Coreference Clusters that the text has
     *
     * @return the coreference clusters
     */
    ImmutableList<ICorefCluster> getCorefClusters();

    /**
     * Returns the sentences of the text, ordered by appearance.
     *
     * @return the sentences of the text, ordered by appearance.
     */
    ImmutableList<ISentence> getSentences();
}
