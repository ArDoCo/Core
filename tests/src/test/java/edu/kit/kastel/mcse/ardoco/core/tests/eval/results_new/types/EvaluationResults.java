package edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.EvaluationResultStringGenerator;

public record EvaluationResults (double precision, double recall, double f1){

    @Override
    public String toString() {
        return EvaluationResultStringGenerator.getResultString(precision, recall, f1);
    }
}
