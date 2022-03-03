/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import java.util.Optional;

/**
 * An interface that allows calculating similarity between two words based on fastText word embeddings.
 */
public interface FastTextDataSource extends AutoCloseable {

    /**
     * Attempts to calculate the similarity between the given words.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return the similarity score, ranging from {@code 0.0} and {@code 1.0}, or {@link Optional#empty()} if at least
     *         one of the given words is not recognized by fastText.
     * @throws RetrieveVectorException if retrieving the vector representation for the given words fails
     */
    Optional<Double> getSimilarity(String firstWord, String secondWord) throws RetrieveVectorException;

    void close();

}
