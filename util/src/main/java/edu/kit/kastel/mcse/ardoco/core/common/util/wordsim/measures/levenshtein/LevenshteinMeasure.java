/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * This word similarity measure uses the levenshtein distance (also sometimes called edit distance) algorithm
 * to calculate word similarity.
 */
public class LevenshteinMeasure implements WordSimMeasure {

    private final LevenshteinDistance distance = new LevenshteinDistance();

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        String original = ctx.firstTerm();
        String word2test = ctx.secondTerm();
        double threshold = CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD; // should probably changed in the future

        String originalLowerCase = original.toLowerCase();
        String word2TestLowerCase = word2test.toLowerCase();

        int areWordsSimilarMinLength = CommonTextToolsConfig.LEVENSHTEIN_MIN_LENGTH;
        int areWordsSimilarMaxLdist = CommonTextToolsConfig.LEVENSHTEIN_MAX_DISTANCE;
        int maxLevenshteinDistance = (int) Math.min(areWordsSimilarMaxLdist, threshold * Math.min(original.length(), word2test.length()));

        int levenshteinDistance = distance.apply(originalLowerCase, word2TestLowerCase);

        if (original.length() <= areWordsSimilarMinLength) {
            var wordsHaveContainmentRelation = word2TestLowerCase.contains(originalLowerCase) || originalLowerCase.contains(word2TestLowerCase);
            if (levenshteinDistance <= areWordsSimilarMaxLdist && wordsHaveContainmentRelation) {
                return true;
            }
        } else if (levenshteinDistance <= maxLevenshteinDistance) {
            return true;
        }
        return false;
    }

}
