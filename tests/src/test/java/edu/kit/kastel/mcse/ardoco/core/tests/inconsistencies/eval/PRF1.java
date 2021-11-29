package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

public class PRF1 implements EvaluationResult {
    private final double precision;
    private final double recall;
    private final double f1;

    PRF1(int tp, int fp, int fn) {
        precision = 1.0 * tp / (tp + fp);
        recall = 1.0 * tp / (tp + fn);
        f1 = 2 * precision * recall / (precision + recall);
    }

    @Override
    public String toString() {
        return String.format("P: %.2f, R: %.2f, F1: %.2f", precision, recall, f1);
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
