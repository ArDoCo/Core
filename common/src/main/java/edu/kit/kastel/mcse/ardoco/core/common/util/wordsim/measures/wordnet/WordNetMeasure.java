/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;

/**
 * A word similarity measure that is using various algorithms on the WordNet graph to calculate word similarity.
 */
public class WordNetMeasure implements WordSimMeasure {

    private final Map<RelatednessCalculator, Double> calcThresholdMap;

    /**
     * Instantiates a new {@link WordNetMeasure}.
     *
     * @param calcThresholdMap a map containing all WordNet algorithms along with their respective similarity
     *                         thresholds.
     */
    public WordNetMeasure(Map<RelatednessCalculator, Double> calcThresholdMap) {
        Objects.requireNonNull(calcThresholdMap);
        this.calcThresholdMap = new HashMap<>(calcThresholdMap);
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        for (RelatednessCalculator calculator : this.calcThresholdMap.keySet()) {
            double threshold = this.calcThresholdMap.get(calculator);

            double similarity = calculator.calcRelatednessOfWords(ctx.firstTerm(), ctx.secondTerm());

            double normalizedSimilarity = (similarity - calculator.getMin()) / calculator.getMax();

            if (normalizedSimilarity >= threshold) {
                return true;
            }
        }

        return false;
    }

}
