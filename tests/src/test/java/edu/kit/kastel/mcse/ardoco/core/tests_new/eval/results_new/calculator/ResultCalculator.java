/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new.calculator;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new.EvaluationResults;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

/**
 *
 * @param <T>     type of the results
 */
public class ResultCalculator <T> {

    private final MutableList<Pair<EvaluationResults<T>, Integer>> resultsWithWeight;

    public ResultCalculator() {
        resultsWithWeight = Lists.mutable.empty();
    }

    /**
     * Adds evaluation results to this calculator
     * 
     * @param results the results
     * @param weight  the weight
     */
    public void addEvaluationResults(EvaluationResults<T> results, int weight) {
        resultsWithWeight.add(Tuples.pair(results, weight));
    }

    /**
     * Returns the weighted average EvaluationResults (Precision, Recall, F1 as {@link EvaluationResults}. Weighted with
     * number of occurrences.
     *
     * @return the weighted EvaluationResults (Precision, Recall, F1 as {@link EvaluationResults}
     */
    public EvaluationResults<T> getWeightedAverageResults() {
        return ResultCalculatorUtil.calculateWeightedAverageResults(resultsWithWeight);
    }

    /**
     * Returns the average EvaluationResults (Precision, Recall, F1).
     *
     * @return the average EvaluationResults (Precision, Recall, F1)
     */
    public EvaluationResults<T> getMacroAverageResults() {
        int nrResults = resultsWithWeight.size();
        return ResultCalculatorUtil.calculateAverageResults(nrResults, resultsWithWeight);
    }

    int getWeight() {
        int weight = 0;
        for (var entry : resultsWithWeight) {
            weight += entry.getTwo();
        }
        return weight;
    }

}
