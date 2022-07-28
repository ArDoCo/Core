/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.List;

/**
 * This class represents explicit evaluation results. The lists containing the true positives, false negatives, and
 * false positives are explicitly stored and can be retrieved, e.g., for analysing purposes.
 * 
 * @param <T> Type of the lists that store true positives, false negatives, and false positives.
 */
public class ExplicitEvaluationResults<T> extends EvaluationResults {

    private double precision = -1.337;
    private double recall = -1.337;
    private double f1 = -1.337;

    private List<T> truePositives;
    private List<T> falseNegatives;
    private List<T> falsePositives;

    /**
     * Construct explicit results by providing the lists containing the true positives, false negatives, and false
     * positives.
     * 
     * @param truePositives  the true positives
     * @param falseNegatives the false negatives
     * @param falsePositives the false positives
     */
    public ExplicitEvaluationResults(List<T> truePositives, List<T> falseNegatives, List<T> falsePositives) {
        this.truePositives = truePositives;
        this.falseNegatives = falseNegatives;
        this.falsePositives = falsePositives;
    }

    /**
     * @return the list of false negatives
     */
    public List<T> getFalseNegative() {
        return falseNegatives;
    }

    /**
     * @return the list of false positives
     */
    public List<T> getFalsePositives() {
        return falsePositives;
    }

    /**
     * @return the list of true positives
     */
    public List<T> getTruePositives() {
        return truePositives;
    }

    @Override
    public double getPrecision() {
        if (precision < 0) {
            double tp = truePositives.size();
            double fp = falsePositives.size();
            precision = tp / (tp + fp);
            if (Double.isNaN(precision)) {
                precision = 0.0;
            }
        }
        return precision;
    }

    @Override
    public double getRecall() {
        if (recall < 0) {
            double tp = truePositives.size();
            double fn = falseNegatives.size();
            recall = tp / (tp + fn);
            if (Double.isNaN(recall)) {
                recall = 0.0;
            }
        }
        return recall;
    }

    @Override
    public double getF1() {
        if (f1 < 0) {
            double precision = getPrecision();
            double recall = getRecall();
            f1 = 2 * precision * recall / (precision + recall);
            if (Double.isNaN(f1)) {
                f1 = 0.0;
            }
        }
        return f1;
    }
}
