package edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResultVector;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import org.eclipse.collections.api.list.MutableList;

public abstract class ResultCalculatorUtil {

    public static <T> EvaluationResults<T> calculateAverageResults(MutableList<EvaluationResults<T>> results) {
        int norm = results.size();
        EvaluationResultVector<T> vector = new EvaluationResultVector<>();

        for (var result: results) {
            var weight = result.getWeight();
            if (weight <= 0) {
                norm--;
                continue;
            }
            vector.add(result);
        }

        vector.scale(norm);
        return vector.toEvaluationResults();
    }

    public static <T> EvaluationResults<T> calculateWeightedAverageResults(MutableList<EvaluationResults<T>> results) {
        int weight = 0;
        EvaluationResultVector<T> vector = new EvaluationResultVector<>();

        for (var result : results) {
            int localWeight = result.getWeight();
            weight += localWeight;
            vector.addWeighted(result, localWeight);
        }

        vector.scale(weight);
        return vector.toEvaluationResults();

    }
}
