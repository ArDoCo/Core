/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler;

import java.io.Serializable;
import java.util.Arrays;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacterMatchFunctions;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacterSequence;

/**
 * A similarity algorithm indicating the percentage of matched characters between two character sequences.
 *
 * <p>
 * The Jaro measure is the weighted sum of percentage of matched characters from each file and transposed characters. Winkler increased this measure for
 * matching initial characters.
 * </p>
 *
 * <p>
 * This implementation is based on the Jaro Winkler similarity algorithm from <a href="http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">
 * http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>.
 * </p>
 *
 * <p>
 * This code is a modified version of {@link org.apache.commons.text.similarity.JaroWinklerSimilarity} provided by Apache Commons Text. It was adapted to use
 * {@link UnicodeCharacter UnicodeCharacters} instead of Java's native {@link Character Characters} and allows for specifying a function that determines a
 * character match.
 * </p>
 */
public final class UnicodeJaroWinklerSimilarity implements Serializable {
    /**
     * This method returns the Jaro-Winkler string matches, half transpositions, prefix array.
     *
     * @param first          the first string to be matched
     * @param second         the second string to be matched
     * @param characterMatch the function used to determine a match between two {@link UnicodeCharacter UnicodeCharacters}
     * @return mtp array containing: matches, half transpositions, and prefix
     */
    private static int[] matches(UnicodeCharacterSequence first, UnicodeCharacterSequence second, UnicodeCharacterMatchFunctions characterMatch) {
        final UnicodeCharacterSequence max;
        final UnicodeCharacterSequence min;
        if (first.length() > second.length()) {
            max = first;
            min = second;
        } else {
            max = second;
            min = first;
        }
        final int range = Math.max(max.length() / 2 - 1, 0);
        final int[] matchIndexes = new int[min.length()];
        Arrays.fill(matchIndexes, -1);
        final boolean[] matchFlags = new boolean[max.length()];
        int matches = 0;
        for (int mi = 0; mi < min.length(); mi++) {
            UnicodeCharacter c1 = min.charAt(mi);
            for (int xi = Math.max(mi - range, 0), xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
                if (!matchFlags[xi] && characterMatch.apply(c1, max.charAt(xi))) {
                    matchIndexes[mi] = xi;
                    matchFlags[xi] = true;
                    matches++;
                    break;
                }
            }
        }
        final UnicodeCharacter[] ms1 = new UnicodeCharacter[matches];
        final UnicodeCharacter[] ms2 = new UnicodeCharacter[matches];
        for (int i = 0, si = 0; i < min.length(); i++) {
            if (matchIndexes[i] != -1) {
                ms1[si] = min.charAt(i);
                si++;
            }
        }
        for (int i = 0, si = 0; i < max.length(); i++) {
            if (matchFlags[i]) {
                ms2[si] = max.charAt(i);
                si++;
            }
        }
        int halfTranspositions = 0;
        for (int mi = 0; mi < ms1.length; mi++) {
            if (!characterMatch.apply(ms1[mi], ms2[mi])) {
                halfTranspositions++;
            }
        }
        int prefix = 0;
        for (int mi = 0; mi < Math.min(4, min.length()); mi++) {
            if (!characterMatch.apply(first.charAt(mi), second.charAt(mi))) {
                break;
            }
            prefix++;
        }
        return new int[] { matches, halfTranspositions, prefix };
    }

    /**
     * Computes the Jaro Winkler Similarity between two character sequences.
     *
     * <pre>
     * sim.apply(null, null) = IllegalArgumentException
     * sim.apply("foo", null) = IllegalArgumentException
     * sim.apply(null, "foo") = IllegalArgumentException
     * sim.apply("", "") = 1.0
     * sim.apply("foo", "foo") = 1.0
     * sim.apply("foo", "foo ") = 0.94
     * sim.apply("foo", "foo ") = 0.91
     * sim.apply("foo", " foo ") = 0.87
     * sim.apply("foo", " foo") = 0.51
     * sim.apply("", "a") = 0.0
     * sim.apply("aaapppp", "") = 0.0
     * sim.apply("frog", "fog") = 0.93
     * sim.apply("fly", "ant") = 0.0
     * sim.apply("elephant", "hippo") = 0.44
     * sim.apply("hippo", "elephant") = 0.44
     * sim.apply("hippo", "zzzzzzzz") = 0.0
     * sim.apply("hello", "hallo") = 0.88
     * sim.apply("ABC Corporation", "ABC Corp") = 0.91
     * sim.apply("D N H Enterprises Inc", "D &amp; H Enterprises, Inc.") = 0.95
     * sim.apply("My Gym Children's Fitness Center", "My Gym. Childrens Fitness") = 0.94
     * sim.apply("PENNSYLVANIA", "PENNCISYLVNIA") = 0.88
     * </pre>
     *
     * @param left           the first UnicodeCharacterSequence, must not be null
     * @param right          the second UnicodeCharacterSequence, must not be null
     * @param characterMatch the function used to determine a match between two {@link UnicodeCharacter UnicodeCharacters}
     * @return result similarity
     * @throws IllegalArgumentException if either CharSequence input is {@code null}
     */
    public static Double apply(UnicodeCharacterSequence left, UnicodeCharacterSequence right, UnicodeCharacterMatchFunctions characterMatch) {
        final double defaultScalingFactor = 0.1;

        if (left == null || right == null) {
            throw new IllegalArgumentException("UnicodeCharSequences must not be null");
        }

        if (left.match(right, characterMatch))
            return 1d;

        final int[] mtp = matches(left, right, characterMatch);
        final double m = mtp[0];
        if (m == 0) {
            return 0d;
        }
        final double j = (m / left.length() + m / right.length() + (m - (double) mtp[1] / 2) / m) / 3;
        return j < 0.7d ? j : j + defaultScalingFactor * mtp[2] * (1d - j);
    }

    /**
     * Computes the Jaro Winkler Similarity between two strings.
     *
     * @param left           the first String, must not be null
     * @param right          the second String, must not be null
     * @param characterMatch the function used to determine a match between two {@link UnicodeCharacter UnicodeCharacters}
     * @return result similarity
     * @throws IllegalArgumentException if either CharSequence input is {@code null}
     */
    public static Double apply(String left, String right, UnicodeCharacterMatchFunctions characterMatch) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        if (left.equals(right))
            return 1d;

        return apply(UnicodeCharacterSequence.valueOf(left), UnicodeCharacterSequence.valueOf(right), characterMatch);
    }
}
