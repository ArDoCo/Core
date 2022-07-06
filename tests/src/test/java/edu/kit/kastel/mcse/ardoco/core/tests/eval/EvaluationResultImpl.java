/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.Locale;

public class EvaluationResultImpl implements EvaluationResult {
    private final double precision;
    private final double recall;
    private final double f1;

    EvaluationResultImpl(int tp, int fp, int fn) {
        var calculatedPrecision = 1.0 * tp / (tp + fp);
        if (Double.isNaN(calculatedPrecision)) {
            this.precision = 0.0;
        } else {
            this.precision = calculatedPrecision;
        }

        var calculatedRecall = 1.0 * tp / (tp + fn);
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

    EvaluationResultImpl(double precision, double recall, double f1) {
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
    }

    EvaluationResultImpl() {
        this.precision = 0;
        this.recall = 0;
        this.f1 = 0;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "P: %.2f, R: %.2f, F1: %.2f", precision, recall, f1);
    }

    /**
     * @return the precision
     */
    @Override
    public double getPrecision() {
        return precision;
    }

    /**
     * @return the recall
     */
    @Override
    public double getRecall() {
        return recall;
    }

    /**
     * @return the f1
     */
    @Override
    public double getF1() {
        return f1;
    }
}
