package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.calculator;

import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.EvaluationResultVector;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.EvaluationResults;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;

public abstract class ResultCalculatorUtil {

    public static <T> EvaluationResults calculateAverageResults(MutableList<Pair<EvaluationResults<T>, Integer>> resultsWithWeight) {
        int norm = resultsWithWeight.size();
        EvaluationResultVector vector = new EvaluationResultVector();

        for (var resultWithWeight : resultsWithWeight) {
            var result = resultWithWeight.getOne();
            var weight = resultWithWeight.getTwo();
            if (weight <= 0) {
                norm--;
                continue;
            }

            vector.add(result);
        }

        vector.scale(norm);
        return vector.toEvaluationResults();
    }

    public static <T> EvaluationResults calculateWeightedAverageResults(MutableList<Pair<EvaluationResults<T>, Integer>> resultsWithWeight) {
        int weight = 0;
        EvaluationResultVector vector = new EvaluationResultVector();

        for (var resultWithWeight : resultsWithWeight) {
            var result = resultWithWeight.getOne();
            int localWeight = resultWithWeight.getTwo();
            weight += localWeight;

            vector.add(result);
            vector.scale(localWeight);
        }

        vector.scale(weight);
        return vector.toEvaluationResults();

    }
}
