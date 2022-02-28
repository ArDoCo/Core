/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.util.ArrayList;
import java.util.List;

public class ComparisonStats {

    public record MeasureResult(WordSimMeasure measure, boolean result) {
    }

    public record Comparison(ComparisonContext ctx, List<MeasureResult> results, boolean finalResult) {
    }

    public record Comp(String firstString, String secondString) {
        public Comp(Comparison comparison) {
            this(comparison.ctx.firstString(), comparison.ctx.secondString());
        }
    }

    public static boolean ENABLED = true;

    public static List<Comparison> COMPARISONS = new ArrayList<>();

    private static ComparisonContext ACTIVE_COMPARISON = null;
    private static List<MeasureResult> RESULTS = new ArrayList<>();

    public static void begin(ComparisonContext ctx) {
        if (!ENABLED) {
            return;
        }

        if (ACTIVE_COMPARISON != null) {
            System.out.println("OH NO");
            System.exit(0);
        }

        ACTIVE_COMPARISON = ctx;
    }

    public static void end(boolean finalResult) {
        if (!ENABLED) {
            return;
        }
        COMPARISONS.add(new Comparison(ACTIVE_COMPARISON, RESULTS.stream().toList(), finalResult));
        ACTIVE_COMPARISON = null;
        RESULTS.clear();
    }

    public static void record(WordSimMeasure measure, boolean result) {
        if (!ENABLED) {
            return;
        }
        RESULTS.add(new MeasureResult(measure, result));
    }

}
