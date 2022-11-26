package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new.calculator;

import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new.EvaluationResults;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;

public abstract class ResultCalculatorUtil {

    public static <T> EvaluationResults<T> calculateAverageResults(int norm, MutableList<Pair<EvaluationResults<T>, Integer>> resultsWithWeight) {
        var precision = 0.0;
        var recall = 0.0;
        var f1 = 0.0;
        double accuracy = 0.0;
        double phi = 0.0;
        double specificity = 0.0;

        double phiMax = 0.0;
        double phiOverPhiMax = 0.0;

        for (var resultWithWeight : resultsWithWeight) {
            var result = resultWithWeight.getOne();
            var weight = resultWithWeight.getTwo();
            if (weight <= 0) {
                norm--;
                continue;
            }

            precision += result.precision();
            recall += result.recall();
            f1 += result.f1();
            accuracy += result.accuracy();
            specificity += result.specificity();
            phi += result.phiCoefficient();

            phiMax += result.phiCoefficientMax();
            phiOverPhiMax += result.phiOverPhiMax();
        }

        precision /= norm;
        recall /= norm;
        f1 /= norm;

        if (phi != 0.0 && accuracy > 0.0) { //TODO: evtl vektor
            phi /= norm;
            phiMax /= norm;
            phiOverPhiMax /= norm;
            accuracy /= norm;
            specificity /= norm;
            return new EvaluationResults<>(precision, recall, f1,
                    Lists.immutable.empty(), Lists.immutable.empty(), Lists.immutable.empty(), Lists.immutable.empty(),
                    accuracy, phi, specificity, phiMax, phiOverPhiMax);
        }
        return new EvaluationResults<>(precision, recall, f1,
                Lists.immutable.empty(), Lists.immutable.empty(), Lists.immutable.empty(), Lists.immutable.empty(),
                0.0, 0.0, 0.0, 0.0, 0.0);

    }

    public static EvaluationResults calculateWeightedAverageResults() {
        return null;
    }
}
