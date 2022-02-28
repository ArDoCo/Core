/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects.
 */
public class NewSimilarityUtils {


    // --- TODO: REMOVE ---------------------------------------------------------------------------------------------------------------------------------------
    static {
        try {
            var list = new ArrayList<WordSimMeasure>();
            list.add(new EqualityMeasure());
            list.add(new LevenshteinMeasure());
            list.add(new JaroWinklerMeasure());
            if (CommonTextToolsConfig.NGRAM_ENABLED) { list.add(new NgramMeasure()); }
            if (CommonTextToolsConfig.SEWORDSIM_ENABLED) { list.add(new SEWordSimMeasure()); }
            init(list);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --------------------------------------------------------------------------------------------------------------------------------------------------------

    private static List<WordSimMeasure> MEASURES;

    public static void init(Collection<WordSimMeasure> measures) {
        MEASURES = new ArrayList<>(measures);
    }

    public static boolean areWordsSimilar(ComparisonContext ctx) {
        Objects.requireNonNull(ctx);

        ComparisonStats.begin(ctx);

        boolean result = false;

        for (WordSimMeasure measure : MEASURES) {
            boolean similar = measure.areWordsSimilar(ctx);

            ComparisonStats.record(measure, similar);

            if (similar) {
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

    // Miscellaneous methods:

    private NewSimilarityUtils() {
    }

}
