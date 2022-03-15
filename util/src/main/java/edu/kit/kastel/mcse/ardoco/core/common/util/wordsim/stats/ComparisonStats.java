/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.ArrayList;
import java.util.List;

public class ComparisonStats {

    public static boolean ENABLED = true;

    private static final List<Comparison> COMPARISONS = new ArrayList<>();
    private static ComparisonContext PENDING_CONTEXT = null;
    private static final List<MeasureResult> PENDING_MEASURES = new ArrayList<>();

    public static void begin(ComparisonContext ctx) {
        if (!ENABLED) {
            return;
        }
        PENDING_CONTEXT = ctx;
        PENDING_MEASURES.clear();
    }

    public static void record(WordSimMeasure measure, boolean accepted) {
        if (!ENABLED) {
            return;
        }
        var measureResult = new MeasureResult(measure, accepted);
        PENDING_MEASURES.add(measureResult);
    }

    public static void end(boolean finalResult) {
        if (!ENABLED) {
            return;
        }
        var comparison = new Comparison(PENDING_CONTEXT, new ArrayList<>(PENDING_MEASURES), finalResult);

        COMPARISONS.add(comparison);

        PENDING_CONTEXT = null;
        PENDING_MEASURES.clear();
    }

    public static void reset() {
        PENDING_CONTEXT = null;
        PENDING_MEASURES.clear();
        COMPARISONS.clear();
    }

    public static List<Comparison> getComparisons() {
        return new ArrayList<>(COMPARISONS);
    }

}
