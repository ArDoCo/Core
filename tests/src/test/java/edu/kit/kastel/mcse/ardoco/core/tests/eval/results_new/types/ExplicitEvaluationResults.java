package edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.EvaluationResultStringGenerator;
import org.eclipse.collections.api.list.ImmutableList;

public record ExplicitEvaluationResults<T> (double precision, double recall, double f1,
                                         ImmutableList<T> truePositives, ImmutableList<T> falseNegatives,
                                         ImmutableList<T> falsePositives){

    @Override
    public String toString() {
        return EvaluationResultStringGenerator.getResultString(precision, recall, f1);
    }
}
