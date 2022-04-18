package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A vector based word similarity measure uses vector embeddings of words to compare their similarity.
 * To get vector embeddings of passed words, a {@link VectorSqliteDatabase} is necessary.
 * Instances of this class additionally manage a cache to improve lookup speeds.
 */
public abstract class VectorBasedWordSimMeasure implements WordSimMeasure {

	private static final float[] ZERO_VECTOR = new float[0];

	private final VectorSqliteDatabase vectorDatabase;
	private final Map<String, float[]> vectorCache = new HashMap<>();

	/**
	 * Constructs a new {@link VectorBasedWordSimMeasure} instance
	 * @param vectorDatabase the vector database used to get vector representations for words
	 */
	protected VectorBasedWordSimMeasure(VectorSqliteDatabase vectorDatabase) {
		this.vectorDatabase = Objects.requireNonNull(vectorDatabase);
	}

	/**
	 * Compares the two given words by computing the cosine similarity between their respective vector representations.
	 * If the vector representation for one of the words is not found, a similarity score of {@code 0.0}
	 * will be returned.
	 * @param firstWord the first word
	 * @param secondWord the second word
	 * @return returns the similarity score between the two words, between 0.0 and 1.0 (inclusive)
	 * @throws SQLException if an error occurs while accessing the vector database
	 */
	public double compareVectors(String firstWord, String secondWord) throws SQLException {
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

	private float[] getVectorFromCacheOrDatabase(String word) throws SQLException {
		float[] vector = this.vectorCache.getOrDefault(word, null);

		if (vector == null) {
			vector = this.vectorDatabase.getWordVector(word).orElse(ZERO_VECTOR);
			this.vectorCache.put(word, vector);
		}

		return vector;
	}

}
