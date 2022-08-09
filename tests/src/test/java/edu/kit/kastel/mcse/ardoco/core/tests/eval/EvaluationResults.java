/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;

/**
 * This class represents evaluation results. Implementing classes should be able to return precision, recall, and
 * F1-score of an evaluation.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Precision_and_recall">Wikipedia: Precision and recall</a>
 */
public class EvaluationResults {
    private UUID id;

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
        this.precision = TestUtil.checkAndRepairPrecision(precision);
        this.recall = TestUtil.checkAndRepairRecall(recall);
        this.f1 = TestUtil.checkAndRepairF1(f1);

        this.id = UUID.randomUUID();
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
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof EvaluationResults other) {
            return this.id.equals(other.id);
        }
        return false;
    }

}
