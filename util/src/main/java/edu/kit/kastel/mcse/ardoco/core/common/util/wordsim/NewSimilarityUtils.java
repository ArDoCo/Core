/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.text.IWord;

import java.util.Objects;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects.
 */
public class NewSimilarityUtils {

    private static WordSimConfig CONFIG = WordSimConfig.DEFAULT;

    public static void setConfig(WordSimConfig config) {
        CONFIG = Objects.requireNonNull(config);
    }

    public static boolean areWordsSimilar(ComparisonContext ctx) {
        Objects.requireNonNull(ctx);

        for (WordSimMeasure measure : CONFIG.getMeasures()) {
            if (measure.areWordsSimilar(ctx)) {
                return true;
            }
        }

        return false;
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

    // Miscellaneous methods:

    private NewSimilarityUtils() {
    }

}
