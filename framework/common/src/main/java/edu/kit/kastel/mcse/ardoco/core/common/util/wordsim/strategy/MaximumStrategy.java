/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

public class MaximumStrategy implements SimilarityStrategy {
    @Override
    public double getSimilarity(ComparisonContext ctx, List<WordSimMeasure> measures) {
        double max = 0.0;

        for (WordSimMeasure measure : measures) {
            var similarity = measure.getSimilarity(ctx);
            if (!Double.isNaN(similarity)) {
                max = Math.max(similarity, max);
            }
        }

        return max;
    }
}
