/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

public class ExtendedEvaluationResultsImpl extends EvaluationResultsImpl implements ExtendedEvaluationResults {

    private double accuracy;
    private double phiCoefficient;
    private double specificity;
    private double phiCoefficientMax;
    private double phiOverPhiMax;

    public ExtendedEvaluationResultsImpl(double precision, double recall, double f1, double accuracy, double phiCoefficient, double phiCoefficientMax,
            double phiOverPhiMax, double specificity) {
        super(precision, recall, f1);
        this.accuracy = accuracy;
        this.phiCoefficient = phiCoefficient;
        this.specificity = specificity;
        this.phiCoefficientMax = phiCoefficientMax;
        this.phiOverPhiMax = phiOverPhiMax;
    }

    public ExtendedEvaluationResultsImpl(double precision, double recall, double f1, double accuracy, double phiCoefficient, double specificity) {
        super(precision, recall, f1);
        this.accuracy = accuracy;
        this.phiCoefficient = phiCoefficient;
        this.specificity = specificity;
        this.phiCoefficientMax = 0.0;
        this.phiOverPhiMax = 0.0;
    }

    @Override
    public double getPhiCoefficient() {
        return this.phiCoefficient;
    }

    @Override
    public double getPhiCoefficientMax() {
        return this.phiCoefficientMax;
    }

    @Override
    public double getPhiOverPhiMax() {
        return this.phiOverPhiMax;
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
