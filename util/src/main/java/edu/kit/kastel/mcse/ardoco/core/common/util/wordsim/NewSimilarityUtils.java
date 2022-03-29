/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats.ComparisonStats;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects.
 */
public class NewSimilarityUtils {

    private static List<WordSimMeasure> MEASURES = WordSimLoader.loadUsingProperties();

    public static void setMeasures(Collection<WordSimMeasure> measures) {
        MEASURES = new ArrayList<>(measures);
    }

    public static boolean areWordsSimilar(ComparisonContext ctx) {
        Objects.requireNonNull(ctx);

        ComparisonStats.begin(ctx);

        boolean result = false;

        for (WordSimMeasure measure : MEASURES) {
            boolean similar = measure.areWordsSimilar(ctx);

            ComparisonStats.record(measure, similar);

            if (similar) { // TODO: Early return here in the future
                result = true;
            }
        }

        ComparisonStats.end(result);

        return result;
    }

    public static boolean areWordsSimilar(String firstWord, String secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false));
    }

    public static boolean areWordsSimilar(IWord firstWord, IWord secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false));
    }

    public static boolean areWordsSimilar(String firstWord, IWord secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false));
    }

    private NewSimilarityUtils() {
    }

}
