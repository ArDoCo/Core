/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * This word similarity measure uses the levenshtein distance (also sometimes called edit distance) algorithm
 * to calculate word similarity.
 */
public class LevenshteinMeasure implements WordSimMeasure {

    private final LevenshteinDistance distance = new LevenshteinDistance();
    private final int minLength;
    private final int maxDistanceWithoutContext;

    public LevenshteinMeasure(int minLength, int maxDistanceWithoutContext) {
        this.minLength = minLength;
        this.maxDistanceWithoutContext = maxDistanceWithoutContext;
    }

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        String firstLowerCase = ctx.firstTerm().toLowerCase();
        String secondLowerCase = ctx.secondTerm().toLowerCase();

        int maxLevenshteinDistance = (int) Math.min(maxDistanceWithoutContext,
                ctx.similarityThreshold() * Math.min(ctx.firstTerm().length(), ctx.secondTerm().length()));

        int levenshteinDistance = distance.apply(firstLowerCase, secondLowerCase);

        if (ctx.firstTerm().length() <= minLength) {
            var wordsHaveContainmentRelation = secondLowerCase.contains(firstLowerCase) || firstLowerCase.contains(secondLowerCase);
            if (levenshteinDistance <= maxLevenshteinDistance && wordsHaveContainmentRelation) {
                return true;
            }
        } else if (levenshteinDistance <= maxLevenshteinDistance) {
            return true;
        }

        return false;
    }

}
