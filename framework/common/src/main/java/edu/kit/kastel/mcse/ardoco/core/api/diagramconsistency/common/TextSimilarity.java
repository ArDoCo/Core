/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import java.util.Map;

import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jgrapht.alg.util.UnorderedPair;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Utility to get the similarity of two texts.
 */
@Deterministic
public final class TextSimilarity {
    private static final Map<UnorderedPair<String, String>, Double> CACHE_FOR_BASIC_FUNCTION = new java.util.LinkedHashMap<>();
    private static final Map<UnorderedPair<String, String>, Double> CACHE_FOR_CASE_INSENSITIVE_FUNCTION = new java.util.LinkedHashMap<>();
    private static final JaroWinklerSimilarity JARO_WINKLER_SIMILARITY = new JaroWinklerSimilarity();
    private static final Map<UnorderedPair<String, String>, Double> CACHE_FOR_JARO_WINKLER_FUNCTION = new java.util.LinkedHashMap<>();
    private static final JaccardSimilarity JACCARD_SIMILARITY = new JaccardSimilarity();
    private static final Map<UnorderedPair<String, String>, Double> CACHE_FOR_JACCARD_FUNCTION = new java.util.LinkedHashMap<>();

    private TextSimilarity() {
    }

    /**
     * Get the similarity of two texts.
     *
     * @param textA
     *              The first text.
     * @param textB
     *              The second text.
     * @return The similarity of the two texts, in the range [0, 1].
     */
    public static double byLevenshtein(String textA, String textB) {
        return CACHE_FOR_BASIC_FUNCTION.computeIfAbsent(new UnorderedPair<>(textA, textB), pair -> {
            LevenshteinDistance algorithm = LevenshteinDistance.getDefaultInstance();

            double maxDistance = Math.max(pair.getFirst().length(), pair.getSecond().length());
            double levenshteinDistance = algorithm.apply(pair.getFirst(), pair.getSecond());

            return 1 - (levenshteinDistance / maxDistance);
        });
    }

    /**
     * Get the similarity of two texts after converting them to lower case.
     *
     * @param textA
     *              The first text.
     * @param textB
     *              The second text.
     * @return The similarity of the two texts, in the range [0, 1].
     */
    public static double byLevenshteinCaseInsensitive(String textA, String textB) {
        return CACHE_FOR_CASE_INSENSITIVE_FUNCTION.computeIfAbsent(new UnorderedPair<>(textA, textB), pair -> {
            LevenshteinDistance algorithm = LevenshteinDistance.getDefaultInstance();

            double maxDistance = Math.max(pair.getFirst().length(), pair.getSecond().length());
            double levenshteinDistance = algorithm.apply(pair.getFirst().toLowerCase(), pair.getSecond().toLowerCase());

            return 1 - (levenshteinDistance / maxDistance);
        });
    }

    /**
     * Get the similarity of two texts using the Jaro-Winkler algorithm after converting them to lower case.
     *
     * @param textA
     *              The first text.
     * @param textB
     *              The second text.
     * @return The similarity of the two texts, in the range [0, 1].
     */
    public static double byJaroWinkler(String textA, String textB) {
        return CACHE_FOR_JARO_WINKLER_FUNCTION.computeIfAbsent(new UnorderedPair<>(textA, textB), pair -> JARO_WINKLER_SIMILARITY.apply(pair.getFirst()
                .toLowerCase(), pair.getSecond().toLowerCase()));
    }

    /**
     * Get the similarity of two texts using the Jaccard algorithm after converting them to lower case.
     *
     * @param textA
     *              The first text.
     * @param textB
     *              The second text.
     * @return The similarity of the two texts, in the range [0, 1].
     */
    public static double byJaccard(String textA, String textB) {
        return CACHE_FOR_JACCARD_FUNCTION.computeIfAbsent(new UnorderedPair<>(textA, textB), pair -> JACCARD_SIMILARITY.apply(pair.getFirst().toLowerCase(),
                pair.getSecond().toLowerCase()));
    }
}
