/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * This class will probably be deleted in the future.
 */
public class ComparisonStats {

    public static boolean ENABLED = true;

    private static final Set<WordPair> KNOWN_WORD_PAIRS = new HashSet<>();
    private static final Set<Comparison> COMPARISONS = new HashSet<>();

    private static class CURRENT {
        static boolean SKIP = false;
        static WordPair WORD_PAIR = null;
        static ComparisonContext CONTEXT = null;
        static double SCORE = Double.NaN;
        static List<MeasureResult> RESULTS = new ArrayList<>();
    }

    public static void begin(ComparisonContext ctx) {
        if (!ENABLED) {
            return;
        }

        CURRENT.WORD_PAIR = new WordPair(ctx.firstTerm(), ctx.secondTerm());
        CURRENT.CONTEXT = ctx;

        if (KNOWN_WORD_PAIRS.contains(CURRENT.WORD_PAIR)) {
            // A previous comparison with the same word pair has already been made
            // => results for the current comparison are already known, this can be skipped
            CURRENT.SKIP = true;
        } else {
            KNOWN_WORD_PAIRS.add(CURRENT.WORD_PAIR);
        }
    }

    public static void record(WordSimMeasure measure, boolean accepted) {
        if (!ENABLED || CURRENT.SKIP) {
            return;
        }

        var measureResult = new MeasureResult(CURRENT.WORD_PAIR, measure, accepted, CURRENT.SCORE);

        CURRENT.RESULTS.add(measureResult);
        CURRENT.SCORE = Double.NaN;
    }

    public static void recordScore(double score) {
        if (!ENABLED || CURRENT.SKIP) {
            return;
        }
        CURRENT.SCORE = score;
    }

    public static void end(boolean finalResult) {
        if (!ENABLED) {
            return;
        }

        if (!CURRENT.SKIP) {
            var comparison = new Comparison(CURRENT.WORD_PAIR, CURRENT.CONTEXT, CURRENT.RESULTS, finalResult);
            COMPARISONS.add(comparison);
        }

        CURRENT.SKIP = false;
        CURRENT.WORD_PAIR = null;
        CURRENT.SCORE = Double.NaN;
        CURRENT.RESULTS.clear();
    }

    public static void reset() {
        COMPARISONS.clear();
        KNOWN_WORD_PAIRS.clear();
        CURRENT.SKIP = false;
        CURRENT.WORD_PAIR = null;
        CURRENT.SCORE = Double.NaN;
        CURRENT.RESULTS.clear();
    }

    public static Set<Comparison> getComparisons() {
        return new HashSet<>(COMPARISONS);
    }

}
