/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.Locale;
import java.util.Objects;

/**
 * This class represents evaluation results. Implementing classes should be able to return precision, recall, and
 * F1-score of an evaluation.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Precision_and_recall">Wikipedia: Precision and recall</a>
 */
public class EvaluationResults {
    private double precision;
    private double recall;
    private double f1;

    protected EvaluationResults() {
        this.precision = 0.0;
        this.recall = 0.0;
        this.f1 = 0.0;
    }

    /**
     * Constructs EvaluationResults, setting precision, recall, and F1.
     * 
     * @param precision the precision
     * @param recall    the recall
     * @param f1        the f1
     */
    public EvaluationResults(double precision, double recall, double f1) {
        if (Double.isNaN(precision)) {
            this.precision = 0.0;
        } else {
            this.precision = precision;
        }

        if (Double.isNaN(recall)) {
            this.recall = 0.0;
        } else {
            this.recall = recall;
        }

        if (Double.isNaN(f1)) {
            this.f1 = 0.0;
        } else {
            this.f1 = f1;
        }
    }

    /**
     * Creates results based on the number of true positives, false positives, and false negatives. Uses the provided
     * data to calculate precision, recall, and F1-score.
     * 
     * @param truePositives  number of true positives
     * @param falsePositives number of false positives
     * @param falseNegatives number of false negatives
     */
    public EvaluationResults(int truePositives, int falsePositives, int falseNegatives) {
        var calculatedPrecision = 1.0 * truePositives / (truePositives + falsePositives);
        if (Double.isNaN(calculatedPrecision)) {
            this.precision = 0.0;
        } else {
            this.precision = calculatedPrecision;
        }

        var calculatedRecall = 1.0 * truePositives / (truePositives + falseNegatives);
        if (Double.isNaN(calculatedRecall)) {
            this.recall = 0.0;
        } else {
            this.recall = calculatedRecall;
        }

        var calculatedF1 = 2 * precision * recall / (precision + recall);
        if (Double.isNaN(calculatedF1)) {
            this.f1 = 0.0;
        } else {
            this.f1 = calculatedF1;
        }
    }

    /**
     * @return the precision
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * @return the recall
     */
    public double getRecall() {
        return recall;
    }

    /**
     * @return the F1-score
     */
    public double getF1() {
        return f1;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Precision: %.3f\tRecall: %.3f\tF1: %.3f", precision, recall, f1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f1, precision, recall);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof EvaluationResults other) {
            return Double.doubleToLongBits(f1) == Double.doubleToLongBits(other.f1)
                    && Double.doubleToLongBits(precision) == Double.doubleToLongBits(other.precision)
                    && Double.doubleToLongBits(recall) == Double.doubleToLongBits(other.recall);
        }
        return false;
    }

}
