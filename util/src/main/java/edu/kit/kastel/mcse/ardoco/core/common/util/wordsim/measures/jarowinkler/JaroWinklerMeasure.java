/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

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
     * Constructs an new {@link JaroWinklerMeasure}.
     * @param similarityThreshold the similarity threshold above which word pairs are considered similar
     */
    public JaroWinklerMeasure(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        return jaroWinklerSimilarity.apply(ctx.firstTerm(), ctx.secondTerm()) >= this.similarityThreshold;
    }

}
