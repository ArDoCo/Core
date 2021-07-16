package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

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
    List<IWord> getWords();
}
