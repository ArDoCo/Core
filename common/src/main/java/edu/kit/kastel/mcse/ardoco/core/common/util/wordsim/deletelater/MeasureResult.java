/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * This class will probably be deleted in the future.
 */
public final class MeasureResult {

    public final WordPair wordPair;
    public final WordSimMeasure measure;
    public final boolean accepted;
    public final double score;

    public MeasureResult(WordPair wordPair, WordSimMeasure measure, boolean accepted) {
        this(wordPair, measure, accepted, Double.NaN);
    }

    public MeasureResult(WordPair wordPair, WordSimMeasure measure, boolean accepted, double score) {
        this.wordPair = wordPair;
        this.measure = measure;
        this.accepted = accepted;
        this.score = score;
    }

    public WordPair wordPair() {
        return wordPair;
    }

    public WordSimMeasure measure() {
        return measure;
    }

    public boolean accepted() {
        return accepted;
    }

    public double score() {
        return score;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (MeasureResult) obj;
        return Objects.equals(this.measure, that.measure) && this.accepted == that.accepted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(measure, accepted);
    }

    @Override
    public String toString() {
        return "MeasureResult[" + "measure=" + measure + ", " + "accepted=" + accepted + ']';
    }

}
