/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EvaluationResults {
    public double precision;
    public double recall;
    public double f1;

    private List<Object> falseNegatives = new ArrayList<>();
    private List<Object> falsePositives = new ArrayList<>();

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

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1() {
        return f1;
    }

    public void setFalseNegatives(List<Object> falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public List<Object> getFalseNegative() {
        return falseNegatives;
    }

    public void setFalsePositives(List<Object> falsePositives) {
        this.falsePositives = falsePositives;
    }

    public List<Object> getFalsePositives() {
        return falsePositives;
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
