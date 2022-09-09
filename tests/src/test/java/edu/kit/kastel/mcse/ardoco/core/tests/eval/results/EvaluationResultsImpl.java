/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import java.util.Locale;

import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;

/**
 * This class implements evaluation results by having the results as data within this class (no direct calculation).
 */
public class EvaluationResultsImpl implements EvaluationResults {
    private final double precision;
    private final double recall;
    private final double f1;

    protected EvaluationResultsImpl() {
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
    public EvaluationResultsImpl(double precision, double recall, double f1) {
        this.precision = TestUtil.checkAndRepairPrecision(precision);
        this.recall = TestUtil.checkAndRepairRecall(recall);
        this.f1 = TestUtil.checkAndRepairF1(f1);
    }

    public EvaluationResultsImpl(int truePositives, int falsePositives, int falseNegatives) {
        this.precision = TestUtil.calculatePrecision(truePositives, falsePositives);
        this.recall = TestUtil.calculateRecall(truePositives, falseNegatives);
        this.f1 = TestUtil.calculateF1(precision, recall);
    }

    @Override
    public double getPrecision() {
        return precision;
    }

    @Override
    public double getRecall() {
        return recall;
    }

    @Override
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
