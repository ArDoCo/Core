/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality;

import java.util.Locale;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacterSequence;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * This word similarity measure just checks whether the most appropriate string representations of the passed objects are equal.
 * Equality of two characters is determined using the provided {@link ComparisonContext#characterMatch() Character Match Function}.
 * Letter-casing is not considered.
 */
public class EqualityMeasure implements WordSimMeasure {

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        var firstTerm = UnicodeCharacterSequence.valueOf(ctx.firstTerm().toLowerCase(Locale.ENGLISH));
        var secondTerm = UnicodeCharacterSequence.valueOf(ctx.secondTerm().toLowerCase(Locale.ENGLISH));
        return firstTerm.match(secondTerm, ctx.characterMatch());
    }

    @Override
    public double getSimilarity(ComparisonContext ctx) {
        return areWordsSimilar(ctx) ? 1 : 0;
    }
}
