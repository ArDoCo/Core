package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector;

import java.util.Objects;

/**
 * Some utility functions for double vectors.
 */
public class VectorUtils {

    /**
     * Calculates the cosine similarity between the two given vectors.
     *
     * @param firstVec  the first vector
     * @param secondVec the second vector
     * @return the cosine similarity
     * @throws IllegalArgumentException if the vectors have different lengths
     */
    public static double cosineSimilarity(double[] firstVec, double[] secondVec) {
        Objects.requireNonNull(firstVec);
        Objects.requireNonNull(secondVec);

        if (firstVec.length != secondVec.length) {
            throw new IllegalArgumentException("vector length does not match!");
        }

        double dotProduct = 0.0, firstNorm = 0.0, secondNorm = 0.0;

        for (int i = 0; i < firstVec.length; i++) {
            dotProduct += firstVec[i] * secondVec[i];
            firstNorm += Math.pow(firstVec[i], 2);
            secondNorm += Math.pow(secondVec[i], 2);
        }

        return dotProduct / (Math.sqrt(firstNorm) * Math.sqrt(secondNorm));
    }

    /**
     * Calculates the cosine similarity between the two given vectors.
     * The two given vectors must have the same length.
     *
     * @param firstVec  the first vector
     * @param secondVec the second vector
     * @return the cosine similarity
     * @throws IllegalArgumentException if the vectors have different lengths
     */
    public static double cosineSimilarity(float[] firstVec, float[] secondVec) {
        Objects.requireNonNull(firstVec);
        Objects.requireNonNull(secondVec);

        if (firstVec.length != secondVec.length) {
            throw new IllegalArgumentException("vector length does not match!");
        }

        double dotProduct = 0.0, firstNorm = 0.0, secondNorm = 0.0;

        for (int i = 0; i < firstVec.length; i++) {
            dotProduct += firstVec[i] * secondVec[i];
            firstNorm += Math.pow(firstVec[i], 2);
            secondNorm += Math.pow(secondVec[i], 2);
        }

        return dotProduct / (Math.sqrt(firstNorm) * Math.sqrt(secondNorm));
    }

    /**
     * Checks whether the given vector contains any non-zero numbers.
     *
     * @param vector the vector to check
     * @return {@code true} if the given vector either has no entries or if the only entries are zero
     */
    public static boolean isZero(double[] vector) {
        Objects.requireNonNull(vector);

        if (vector.length <= 0) {
            return true;
        }

        for (double entry : vector) {
            if (entry != 0.0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds the given vectors together and stores the resulting vector in the first argument.
     * The two given vectors must have the same length.
     *
     * @param result the first vector that will also be the result vector after the addition
     * @param toAdd  the second vector
     */
    public static void add(double[] result, double[] toAdd) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(toAdd);

        if (result.length != toAdd.length) {
            throw new IllegalArgumentException("vectors of different lengths: " + result.length + " != " + toAdd.length);
        }

        for (int i = 0; i < result.length; i++) {
            result[i] += toAdd[i];
        }
    }

    /**
     * Scales the given vector by the given scalar.
     *
     * @param vector the vector to scale
     * @param scalar the scalar
     */
    public static void scale(double[] vector, double scalar) {
        Objects.requireNonNull(vector);

        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] * scalar;
        }
    }

}
