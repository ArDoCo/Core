package edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.EvaluationResultStringGenerator;

public record ExtendedEvaluationResults (double precision, double recall, double f1,
                                         double accuracy, double phiCoefficient, double specificity,
                                         double phiCoefficientMax, double phiOverPhiMax){

    @Override
    public String toString() {
        return EvaluationResultStringGenerator.getExtendedResultString(precision, recall, f1,
                accuracy, specificity, phiCoefficient, phiOverPhiMax, phiCoefficientMax);
    }
}
