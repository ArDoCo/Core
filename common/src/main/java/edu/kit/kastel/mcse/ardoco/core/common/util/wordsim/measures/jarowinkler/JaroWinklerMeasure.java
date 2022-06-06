/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * This word similarity measure uses the jaro winkler algorithm to calculate similarity.
 */
public class JaroWinklerMeasure implements WordSimMeasure {

    private final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();

    private final double similarityThreshold;

    /**
     * Constructs a new {@link JaroWinklerMeasure} using the settings provided by {@link CommonTextToolsConfig}.
     */
    public JaroWinklerMeasure() {
        this(CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link JaroWinklerMeasure}.
     * 
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     * @throws IllegalArgumentException if the given threshold is not between 0 and 1
     */
    public JaroWinklerMeasure(double similarityThreshold) throws IllegalArgumentException {
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold outside of valid range: " + similarityThreshold);
        }

        this.similarityThreshold = similarityThreshold;
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        double similarity = this.jaroWinklerSimilarity.apply(ctx.firstTerm(), ctx.secondTerm());
        return similarity >= this.similarityThreshold;
    }

}
