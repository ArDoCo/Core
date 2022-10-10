/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

public class ExtendedEvaluationResultsImpl extends EvaluationResultsImpl implements ExtendedEvaluationResults {

    private double accuracy;
    private double phiCoefficient;
    private double specificity;

    public ExtendedEvaluationResultsImpl(double precision, double recall, double f1, double accuracy, double phiCoefficient, double specificity) {
        super(precision, recall, f1);
        this.accuracy = accuracy;
        this.phiCoefficient = phiCoefficient;
        this.specificity = specificity;
    }

    @Override
    public double getPhiCoefficient() {
        return this.phiCoefficient;
    }

    @Override
    public double getAccuracy() {
        return this.accuracy;
    }

    @Override
    public double getSpecificity() {
        return this.specificity;
    }

    @Override
    public String toString() {
        return this.getResultString();
    }
}
