/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import info.debatty.java.stringsimilarity.NGram;

/**
 * This word similarity measure uses the N-gram word distance function defined by Kondrak 2005.
 */
public class NgramMeasure implements WordSimMeasure {

    private final NGram nGram;
    private final double weight;

    /**
     * Constructs a new {@link NgramMeasure} instance.
     *
     * @param n the length of each gram
     */
    public NgramMeasure(int n, double weight) {
        this.nGram = new NGram(n);
        this.weight = weight;
    }

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        return calculateDistance(ctx.firstString(), ctx.secondString()) * weight >= ctx.similarityThreshold();
    }

    public double calculateDistance(String firstString, String secondString) {
        return this.nGram.distance(firstString, secondString); // already normalized
    }

}
