/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.util.Locale;
import java.util.Objects;

class EvaluationResults {
    public double precision;
    public double recall;
    public double f1;

    public EvaluationResults(double precision, double recall, double f1) {
        super();
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1() {
        return f1;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Precision: %.3f\tRecall: %.3f\tF1: %.3f", precision, recall, f1);
    }

    public String toPrettyString() {
        return String.format(Locale.US, "\tPrecision:\t%.3f%n\tRecall:\t\t%.3f%n\tF1:\t\t%.3f", precision, recall, f1);
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
        if (!(obj instanceof EvaluationResults)) {
            return false;
        }
        EvaluationResults other = (EvaluationResults) obj;
        return Double.doubleToLongBits(f1) == Double.doubleToLongBits(other.f1)
                && Double.doubleToLongBits(precision) == Double.doubleToLongBits(other.precision)
                && Double.doubleToLongBits(recall) == Double.doubleToLongBits(other.recall);
    }

}
