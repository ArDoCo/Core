/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

public class AverageStrategy implements SimilarityStrategy {
    @Override
    public double getSimilarity(ComparisonContext ctx, List<WordSimMeasure> measures) {
        double sum = 0.0;
        int successful = 0;

        for (WordSimMeasure measure : measures) {
            var similarity = measure.getSimilarity(ctx);
            if (!Double.isNaN(similarity)) {
                successful++;
                sum += similarity;
            }
        }

        return successful == 0 ? 0.0 : sum / successful;
    }
}
