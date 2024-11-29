/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.vector;

/**
 * An exception that can occur while trying to retrieve a vector.
 */
public class RetrieveVectorException extends Exception {

    public RetrieveVectorException(String message, Throwable cause) {
        super(message, cause);
    }

}
