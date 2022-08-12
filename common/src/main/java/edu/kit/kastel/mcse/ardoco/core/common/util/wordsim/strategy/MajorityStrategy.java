/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * This comparison strategy accepts any word pair as similar if the majority of specified word similarity measures
 * accept the word pair as similar.
 */
public class MajorityStrategy implements ComparisonStrategy {

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx, List<WordSimMeasure> measures) {
        int acceptances = 0;

        for (WordSimMeasure measure : measures) {
            if (measure.areWordsSimilar(ctx)) {
                acceptances++;
            }
        }

        return acceptances > (measures.size() / 2);
    }

}
