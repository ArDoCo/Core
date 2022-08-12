/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * This word similarity measure just checks whether the most appropriate string representations of the passed objects
 * are equal.
 */
public class EqualityMeasure implements WordSimMeasure {

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        return ctx.firstTerm().equalsIgnoreCase(ctx.secondTerm());
    }

}
