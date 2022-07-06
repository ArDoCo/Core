package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.List;

public class ExplicitEvaluationResults extends EvaluationResults {

    private double precision = -1.337;
    private double recall = -1.337;
    private double f1 = -1.337;

    private List<? extends Object> truePositives;
    private List<? extends Object> falseNegatives;
    private List<? extends Object> falsePositives;

    public ExplicitEvaluationResults(List<? extends Object> truePositives, List<? extends Object> falseNegatives, List<? extends Object> falsePositives) {
        this.truePositives = truePositives;
        this.falseNegatives = falseNegatives;
        this.falsePositives = falsePositives;
    }

    public List<? extends Object> getFalseNegative() {
        return falseNegatives;
    }

    public List<? extends Object> getFalsePositives() {
        return falsePositives;
    }

    public List<? extends Object> getTruePositives() {
        return truePositives;
    }

    @Override
    public double getPrecision() {
        if (precision < 0) {
            double tp = truePositives.size();
            double fp = falsePositives.size();
            precision = tp / (tp + fp);
        }
        return precision;
    }

    @Override
    public double getRecall() {
        if (recall < 0) {
            double tp = truePositives.size();
            double fn = falseNegatives.size();
            recall = tp / (tp + fn);
        }
        return recall;
    }

    @Override
    public double getF1() {
        if (f1 < 0) {
            double precision = getPrecision();
            double recall = getRecall();
            f1 = 2 * precision * recall / (precision + recall);
        }
        return f1;
    }
}
