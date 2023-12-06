/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;

/**
 * General class to contain metrics for classification.
 */
public interface Metrics {
    /**
     * Get the number of true positives.
     *
     * @return The number of true positives.
     */
    int getTruePositiveCount();

    /**
     * Get the number of false positives.
     *
     * @return The number of false positives.
     */
    int getFalsePositiveCount();

    /**
     * Get the number of false negatives.
     *
     * @return The number of false negatives.
     */
    int getFalseNegativeCount();

    /**
     * Get the precision of the matching.
     *
     * @return The precision.
     */
    default double getPrecision() {
        return EvaluationMetrics.calculatePrecision(this.getTruePositiveCount(), this.getFalsePositiveCount());
    }

    /**
     * Get the recall of the matching.
     *
     * @return The recall.
     */
    default double getRecall() {
        return EvaluationMetrics.calculateRecall(this.getTruePositiveCount(), this.getFalseNegativeCount());
    }

    /**
     * Get the F1 score of the matching.
     *
     * @return The F1 score.
     */
    default double getF1Score() {
        return EvaluationMetrics.calculateF1(this.getTruePositiveCount(), this.getFalsePositiveCount(), this.getFalseNegativeCount());
    }
}
