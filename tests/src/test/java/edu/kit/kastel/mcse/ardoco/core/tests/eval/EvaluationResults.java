/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;

import java.util.Locale;

/**
 * This class represents evaluation results. Implementing classes should be able to return precision, recall, and
 * F1-score of an evaluation.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Precision_and_recall">Wikipedia: Precision and recall</a>
 */
public class EvaluationResults {
    private final double precision;
    private final double recall;
    private final double f1;

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
        this.precision = TestUtil.checkAndRepairPrecision(precision);
        this.recall = TestUtil.checkAndRepairRecall(recall);
        this.f1 = TestUtil.checkAndRepairF1(f1);
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
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }
}
