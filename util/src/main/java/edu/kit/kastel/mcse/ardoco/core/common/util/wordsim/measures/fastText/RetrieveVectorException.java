/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

public class RetrieveVectorException extends Exception {

    public RetrieveVectorException(String word, Throwable cause) {
        super("Failed to retrieve vector for: " + word, cause);
    }

    public RetrieveVectorException(Throwable cause) {
        super(cause);
    }

}
