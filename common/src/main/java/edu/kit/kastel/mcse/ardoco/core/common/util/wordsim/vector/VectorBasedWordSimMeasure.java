/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * A vector based word similarity measure uses vector embeddings of words to compare their similarity. To get vector
 * embeddings of passed words, a {@link WordVectorDataSource} is required. Instances of this class additionally manage a
 * cache to improve lookup speeds.
 */
public abstract class VectorBasedWordSimMeasure implements WordSimMeasure {

    private static final float[] ZERO_VECTOR = new float[0];

    private final WordVectorDataSource vectorDataSource;
    private final Map<String, float[]> vectorCache = new HashMap<>();

    /**
     * Constructs a new {@link VectorBasedWordSimMeasure} instance
     * 
     * @param vectorDataSource the vector database used to get vector representations for words
     */
    protected VectorBasedWordSimMeasure(WordVectorDataSource vectorDataSource) {
        this.vectorDataSource = Objects.requireNonNull(vectorDataSource);
    }

    /**
     * Compares the two given words by computing the cosine similarity between their respective vector representations.
     * If the vector representation for one of the words is not found, a similarity score of {@code 0.0} will be
     * returned.
     * 
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return returns the similarity score between the two words, between 0.0 and 1.0 (inclusive)
     * @throws RetrieveVectorException if an error occurs while retrieving the word vectors
     */
    public double compareVectors(String firstWord, String secondWord) throws RetrieveVectorException {
        Objects.requireNonNull(firstWord);
        Objects.requireNonNull(secondWord);

        if (firstWord.equals(secondWord)) {
            return 1.0;
        }

        float[] firstVec = getVectorFromCacheOrDatabase(firstWord);

        if (VectorUtils.isZero(firstVec)) {
            return 0.0; // no vector representation for the first word
        }

        float[] secondVec = getVectorFromCacheOrDatabase(secondWord);

        if (VectorUtils.isZero(secondVec)) {
            return 0.0; // no vector representation for the second word
        }

        return VectorUtils.cosineSimilarity(firstVec, secondVec);
    }

    private float[] getVectorFromCacheOrDatabase(String word) throws RetrieveVectorException {
        float[] vector = this.vectorCache.getOrDefault(word, null);

        if (vector == null) {
            vector = this.vectorDataSource.getWordVector(word).orElse(ZERO_VECTOR);
            this.vectorCache.put(word, vector);
        }

        return vector;
    }

}
