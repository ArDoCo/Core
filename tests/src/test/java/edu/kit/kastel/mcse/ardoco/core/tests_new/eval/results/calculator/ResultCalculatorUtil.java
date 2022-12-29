package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.calculator;

import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.EvaluationResultVector;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.EvaluationResults;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;

public abstract class ResultCalculatorUtil {

    /**
     * TODO ist weight immer gleich result.getWeight()?
     * @param results
     * @return
     * @param <T>
     */
    public static <T> EvaluationResults calculateAverageResults(MutableList<EvaluationResults<T>> results) {
        int norm = results.size();
        EvaluationResultVector vector = new EvaluationResultVector();

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
        EvaluationResultVector vector = new EvaluationResultVector();

        for (var result : results) {
            int localWeight = result.getWeight();
            weight += localWeight;
            vector.addWeighted(result, localWeight);
        }

        vector.scale(weight);
        return vector.toEvaluationResults();

    }
}
