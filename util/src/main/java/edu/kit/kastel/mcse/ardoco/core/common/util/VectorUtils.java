package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.util.Objects;

public class VectorUtils {

    // --- FOR DOUBLES -----------------------------------------------------------

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

    public static boolean isZero(double[] vector) {
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

}
