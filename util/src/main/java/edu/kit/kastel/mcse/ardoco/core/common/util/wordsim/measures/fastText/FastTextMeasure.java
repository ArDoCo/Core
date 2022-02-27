/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import java.nio.file.Path;

import org.deeplearning4j.models.fasttext.FastText;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * A word similarity measure based on the fastText neural network.
 */
public class FastTextMeasure implements WordSimMeasure {

    private final FastText fastText;
    private final double similarityThreshold;

    /**
     * Constructs a new {@link FastTextMeasure} instance.
     * 
     * @param similarityThreshold the treshold above which words are considered similar
     */
    public FastTextMeasure(Path trainedModelPath, double similarityThreshold) {
        this.fastText = new FastText(trainedModelPath.toFile());
        this.similarityThreshold = similarityThreshold;
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        // TODO: Check if this even utilizes the ngram feature
        return this.fastText.similarity(ctx.firstTerm(), ctx.secondTerm()) >= this.similarityThreshold;
    }

}
