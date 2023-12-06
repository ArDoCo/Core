/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jgrapht.alg.util.UnorderedPair;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * A text similarity function that assigns weights to specific words.
 */
@Deterministic
public class WeightedTextSimilarity {
    private static final String ALPHA_NUMERIC_PATTERN = "[a-zA-Z0-9]+";
    private final Map<UnorderedPair<String, String>, Double> cache = new java.util.LinkedHashMap<>();
    private final Map<String, Double> weights;

    /**
     * Create a new weighted text similarity function.
     *
     * @param weights
     *                The weights to use for words.
     */
    public WeightedTextSimilarity(Map<String, Double> weights) {
        this.weights = weights;
    }

    /**
     * Get all words in a text or identifier, taking camel case into account.
     *
     * @param text
     *             The text.
     * @return A stream of words.
     */
    public static Stream<String> getWords(String text) {
        return Arrays.stream(StringUtils.splitByCharacterTypeCamelCase(text)).filter(s -> s.matches(ALPHA_NUMERIC_PATTERN)).map(String::toLowerCase);
    }

    private static double getMaxLength(List<String> words) {
        double length = 0.0;
        for (String word : words) {
            length = Math.max(length, word.length());
        }
        return length;
    }

    /**
     * Get the similarity of two texts using a Jaccard-based algorithm operating on words after converting them to lower
     * case. Word equality is determined using the levenshtein distance.
     *
     * @param textA
     *              The first text.
     * @param textB
     *              The second text.
     * @return The similarity of the two texts, in the range [0, 1].
     */
    public double apply(String textA, String textB) {
        return this.cache.computeIfAbsent(new UnorderedPair<>(textA, textB), pair -> this.calculate(pair.getFirst(), pair.getSecond()));
    }

    private double calculate(String textA, String textB) {
        List<String> a = getWords(textA).toList();
        List<String> b = getWords(textB).toList();

        if (a.size() <= 1 && b.size() <= 1) {
            return TextSimilarity.byLevenshteinCaseInsensitive(textA, textB);
        }

        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }

        double maxA = getMaxLength(a);
        double maxB = getMaxLength(b);

        if (maxA == 0 || maxB == 0) {
            return 0.0;
        }

        double sizeA = this.getSimilaritySize(a, maxA);
        double sizeB = this.getSimilaritySize(b, maxB);

        double intersection = this.getSimilaritySize(a, maxA, b, maxB);
        double union = sizeA + sizeB - intersection;

        if (union == 0) {
            return 1.0;
        }

        return intersection / union;
    }

    private double getSimilaritySize(List<String> a, double longestInA) {
        return this.getSimilaritySize(a, longestInA, a, longestInA);
    }

    private double getSimilaritySize(List<String> a, double maxA, List<String> b, double maxB) {
        double size = 0.0;
        for (String wordA : a) {
            double lengthWeightA = wordA.length() / maxA;
            double assignedWeightA = this.weights.getOrDefault(wordA, 1.0);
            for (String wordB : b) {
                double lengthWeightB = wordB.length() / maxB;
                double assignedWeightB = this.weights.getOrDefault(wordB, 1.0);
                size += TextSimilarity.byLevenshtein(wordA, wordB) * lengthWeightA * lengthWeightB * assignedWeightA * assignedWeightB;
            }
        }
        return size;
    }
}
