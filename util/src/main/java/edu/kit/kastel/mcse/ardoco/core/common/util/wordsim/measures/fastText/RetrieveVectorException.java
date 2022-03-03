/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

public class RetrieveVectorException extends Exception {

    private final String word;

    public RetrieveVectorException(String word, Throwable cause) {
        super("Failed to retrieve vector for: " + word, cause);
        this.word = word;
    }

    public String getWord() {
        return word;
    }

}
