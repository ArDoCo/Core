/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.text.IWord;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects.
 */
public class NewSimilarityUtils {

    private static WordSimConfig CONFIG = WordSimConfig.DEFAULT;

    public static void setConfig(WordSimConfig config) {
        CONFIG = config;
    }

    public static boolean areWordsSimilar(ComparisonContext ctx) {
        for (WordSimMeasure measure : CONFIG.getMeasures()) {
            if (measure.areWordsSimilar(ctx)) {
                return true;
            }
        }

        return false;
    }

    // Overloading methods with similarityThreshold:

    public static boolean areWordsSimilar(String firstWord, String secondWord, double similarityThreshold) {
        var ctx = new ComparisonContext(similarityThreshold, firstWord, secondWord, false);
        return areWordsSimilar(ctx);
    }

    public static boolean areWordsSimilar(IWord firstWord, IWord secondWord, double similarityThreshold) {
        var ctx = new ComparisonContext(similarityThreshold, firstWord, secondWord, false);
        return areWordsSimilar(ctx);
    }

    public static boolean areWordsSimilar(String firstWord, IWord secondWord, double similarityThreshold) {
        var ctx = new ComparisonContext(similarityThreshold, firstWord, secondWord.getText(), null, secondWord, null, null, false);
        return areWordsSimilar(ctx);
    }

    // Overloading methods without similarityThreshold:

    public static boolean areWordsSimilar(String firstWord, String secondWord) {
        return areWordsSimilar(firstWord, secondWord, CONFIG.getDefaultSimilarityThreshold());
    }

    public static boolean areWordsSimilar(IWord firstWord, IWord secondWord) {
        var ctx = new ComparisonContext(CONFIG.getDefaultSimilarityThreshold(), firstWord, secondWord, false);
        return areWordsSimilar(ctx);
    }

    public static boolean areWordsSimilar(String firstWord, IWord secondWord) {
        var ctx = new ComparisonContext(CONFIG.getDefaultSimilarityThreshold(), firstWord, secondWord.getText(), null, secondWord, null, null, false);
        return areWordsSimilar(ctx);
    }

    // Misc methods:

    private NewSimilarityUtils() {
    }

}
