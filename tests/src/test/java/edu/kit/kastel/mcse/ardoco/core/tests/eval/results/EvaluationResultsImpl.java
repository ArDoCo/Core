/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;

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
        this.precision = EvaluationMetrics.checkAndRepairPrecision(precision);
        this.recall = EvaluationMetrics.checkAndRepairRecall(recall);
        this.f1 = EvaluationMetrics.checkAndRepairF1(f1);
    }

    public EvaluationResultsImpl(int truePositives, int falsePositives, int falseNegatives) {
        this.precision = EvaluationMetrics.calculatePrecision(truePositives, falsePositives);
        this.recall = EvaluationMetrics.calculateRecall(truePositives, falseNegatives);
        this.f1 = EvaluationMetrics.calculateF1(precision, recall);
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
        return this.getResultString();
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
