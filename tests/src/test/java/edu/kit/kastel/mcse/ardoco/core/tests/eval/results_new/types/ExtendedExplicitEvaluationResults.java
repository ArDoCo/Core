package edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.EvaluationResultStringGenerator;
import org.eclipse.collections.api.list.ImmutableList;

public record ExtendedExplicitEvaluationResults<T> (double precision, double recall, double f1,
                                                    ImmutableList<T> truePositives, ImmutableList<T> falseNegatives,
                                                    ImmutableList<T> falsePositives,
                                                    double accuracy, double phiCoefficient, double specificity,
                                                    double phiCoefficientMax, double phiOverPhiMax,
                                                    int trueNegatives) {

    @Override
    public String toString() {
        return EvaluationResultStringGenerator.getResultString(precision, recall, f1);
    }

}
