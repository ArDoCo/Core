/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

/**
 * This class provides metrics on given integer values for true positives, false positives and false negatives.
 *
 * @param truePositives
 *                       The number of true positives.
 * @param falsePositives
 *                       The number of false positives.
 * @param falseNegatives
 *                       The number of false negatives.
 */
public record IntegerMetrics(int truePositives, int falsePositives, int falseNegatives) implements Metrics {

    @Override
    public int getTruePositiveCount() {
        return this.truePositives;
    }

    @Override
    public int getFalsePositiveCount() {
        return this.falsePositives;
    }

    @Override
    public int getFalseNegativeCount() {
        return this.falseNegatives;
    }
}
