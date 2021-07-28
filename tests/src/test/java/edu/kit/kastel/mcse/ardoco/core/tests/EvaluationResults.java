package edu.kit.kastel.mcse.ardoco.core.tests;

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

}
