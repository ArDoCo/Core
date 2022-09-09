package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;

public class ExtendedEvaluationResultsImpl extends EvaluationResultsImpl implements ExtendedEvaluationResults {

    private double accuracy;
    private double phiCoefficient;

    public ExtendedEvaluationResultsImpl(int truePositives, int falsePositives, int falseNegatives, int trueNegatives) {
        super(truePositives, falsePositives, falseNegatives);

        this.accuracy = TestUtil.calculateAccuracy(truePositives, falsePositives, falseNegatives, trueNegatives);
        this.phiCoefficient = TestUtil.calculatePhiCoefficient(truePositives, falsePositives, falseNegatives, trueNegatives);
    }

    public ExtendedEvaluationResultsImpl(double precision, double recall, double f1, double accuracy, double phiCoefficient) {
        super(precision, recall, f1);
        this.accuracy = accuracy;
        this.phiCoefficient = phiCoefficient;
    }

    @Override
    public double getPhiCoefficient() {
        return this.phiCoefficient;
    }

    @Override
    public double getAccuracy() {
        return this.accuracy;
    }
}
