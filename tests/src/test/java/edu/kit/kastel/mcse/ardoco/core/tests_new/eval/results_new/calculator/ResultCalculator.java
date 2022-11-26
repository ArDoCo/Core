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
        int weight = 0;
        double precision = 0.0;
        double recall = 0.0;
        double f1 = 0.0;
        double accuracy = 0.0;
        double phi = 0.0;
        double specificity = 0.0;

        double phiMax = 0.0;
        double phiOverPhiMax = 0.0;

        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        for (var resultWithWeight : resultsWithWeight) {
            var result = resultWithWeight.getOne();
            int localWeight = resultWithWeight.getTwo();
            weight += localWeight;

            precision += result.precision() * localWeight;
            recall += result.recall() * localWeight;
            f1 +=  result.f1() * localWeight;
            accuracy += result.accuracy() * localWeight;
            specificity += result.specificity() * localWeight;
            phi += result.phiCoefficient() * localWeight;

            phiMax += result.phiCoefficientMax() * localWeight;
            phiOverPhiMax += result.phiOverPhiMax() * localWeight;

            truePositives += result.truePositives().size();
            falseNegatives += result.falseNegatives().size();
            falsePositives += result.falsePositives().size();
            trueNegatives += result.trueNegatives();

        }

        precision /= weight;
        recall /= weight;
        f1 /= weight;

        if (truePositives > 0) {
            phi = EvaluationMetrics.calculatePhiCoefficient(truePositives, falsePositives, falseNegatives, trueNegatives);
            phiMax = EvaluationMetrics.calculatePhiCoefficientMax(truePositives, falsePositives, falseNegatives, trueNegatives);
            phiOverPhiMax = EvaluationMetrics.calculatePhiOverPhiMax(truePositives, falsePositives, falseNegatives, trueNegatives);
            accuracy = accuracy / weight;
            specificity = specificity / weight;
            return new EvaluationResults<T>(precision, recall, f1,
                    Lists.immutable.empty(), 0, Lists.immutable.empty(), Lists.immutable.empty(),
                    accuracy, phi, specificity, phiMax, phiOverPhiMax);
        }
        if (phi != 0.0 && accuracy > 0.0) {
            phi = phi / weight;
            phiMax = phiMax / weight;
            phiOverPhiMax = phiOverPhiMax / weight;
            accuracy = accuracy / weight;
            specificity = specificity / weight;
            return new EvaluationResults<T>(precision, recall, f1,
                    Lists.immutable.empty(), 0, Lists.immutable.empty(), Lists.immutable.empty(),
                    accuracy, phi, specificity, phiMax, phiOverPhiMax);
        }
        return new EvaluationResults<T>(precision, recall, f1,
                Lists.immutable.empty(), 0, Lists.immutable.empty(), Lists.immutable.empty(),
                0.0, 0.0, 0.0, 0.0, 0.0);
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
