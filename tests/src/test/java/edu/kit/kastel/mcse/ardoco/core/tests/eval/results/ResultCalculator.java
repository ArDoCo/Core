/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;

public class ResultCalculator {

    private MutableList<Pair<EvaluationResults, Integer>> resultsWithWeight;

    public ResultCalculator() {
        resultsWithWeight = Lists.mutable.empty();
    }

    /**
     * Adds evaluation results to this calculator
     * 
     * @param results the results
     * @param weight  the weight
     * @param <T>     type of the results, extending {@link EvaluationResults}
     */
    public <T extends EvaluationResults> void addEvaluationResults(T results, int weight) {
        if (results instanceof ExtendedEvaluationResults extendedResults) {
            resultsWithWeight.add(Tuples.pair(extendedResults, weight));
        } else {
            resultsWithWeight.add(Tuples.pair(results, weight));
        }
    }

    /**
     * Returns the weighted average EvaluationResults (Precision, Recall, F1 as {@link EvaluationResultsImpl}. Weighted with
     * number of occurrences.
     *
     * @return the weighted EvaluationResults (Precision, Recall, F1 as {@link EvaluationResultsImpl}
     */
    public EvaluationResults getWeightedAverageResults() {
        int weight = 0;
        double precision = 0.0;
        double recall = 0.0;
        double f1 = 0.0;

        double accuracy = 0.0;
        double specificity = 0.0;
        double phi = 0.0;
        double phiMax = 0.0;
        double phiOverPhiMax = 0.0;
        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        for (var resultWithWeight : resultsWithWeight) {
            var result = resultWithWeight.getOne();
            int localWeight = resultWithWeight.getTwo();

            double localPrecision = result.getPrecision();
            double localRecall = result.getRecall();
            double localF1 = result.getF1();
            if (!Double.isNaN(localPrecision) && !Double.isNaN(localRecall) && !Double.isNaN(localF1)) {
                precision += (localWeight * localPrecision);
                recall += (localWeight * localRecall);
                f1 += (localWeight * localF1);
                weight += localWeight;
            }

            if (result instanceof ExtendedExplicitEvaluationResults<?> extendedExplicitResults) {
                truePositives += extendedExplicitResults.getTruePositives().size();
                falseNegatives += extendedExplicitResults.getFalseNegatives().size();
                falsePositives += extendedExplicitResults.getFalsePositives().size();
                trueNegatives += extendedExplicitResults.getNumberOfTrueNegatives();

                accuracy += extendedExplicitResults.getAccuracy() * localWeight;
                specificity += extendedExplicitResults.getSpecificity() * localWeight;
            } else if (result instanceof ExtendedEvaluationResults extendedResult) {
                phi += extendedResult.getPhiCoefficient() * localWeight;
                phiMax += extendedResult.getPhiCoefficientMax() * localWeight;
                phiOverPhiMax += extendedResult.getPhiOverPhiMax() * localWeight;
                accuracy += extendedResult.getAccuracy() * localWeight;
                specificity += extendedResult.getSpecificity() * localWeight;
            }
        }

        precision = precision / weight;
        recall = recall / weight;
        f1 = f1 / weight;

        if (truePositives > 0) {
            phi = EvaluationMetrics.calculatePhiCoefficient(truePositives, falsePositives, falseNegatives, trueNegatives);
            phiMax = EvaluationMetrics.calculatePhiCoefficientMax(truePositives, falsePositives, falseNegatives, trueNegatives);
            phiOverPhiMax = EvaluationMetrics.calculatePhiOverPhiMax(truePositives, falsePositives, falseNegatives, trueNegatives);
            accuracy = accuracy / weight;
            specificity = specificity / weight;
            return new ExtendedEvaluationResultsImpl(precision, recall, f1, accuracy, phi, phiMax, phiOverPhiMax, specificity);
        }
        if (phi != 0.0 && accuracy > 0.0) {
            phi = phi / weight;
            phiMax = phiMax / weight;
            phiOverPhiMax = phiOverPhiMax / weight;
            accuracy = accuracy / weight;
            specificity = specificity / weight;
            return new ExtendedEvaluationResultsImpl(precision, recall, f1, accuracy, phi, phiMax, phiOverPhiMax, specificity);
        }
        return new EvaluationResultsImpl(precision, recall, f1);
    }

    /**
     * Returns the average EvaluationResults (Precision, Recall, F1 as {@link EvaluationResultsImpl}.
     *
     * @return the average EvaluationResults (Precision, Recall, F1 as {@link EvaluationResultsImpl}
     */
    public EvaluationResultsImpl getMacroAverageResults() {
        var precision = 0.0;
        var recall = 0.0;
        var f1 = 0.0;

        double accuracy = 0.0;
        double specificity = 0.0;
        double phi = 0.0;
        double phiMax = 0.0;
        double phiOverPhiMax = 0.0;

        var counter = 0;
        for (var resultWithWeight : resultsWithWeight) {
            var result = resultWithWeight.getOne();
            var weight = resultWithWeight.getTwo();
            if (weight == 0) {
                continue;
            }

            var localPrecision = result.getPrecision();
            var localRecall = result.getRecall();
            var localF1 = result.getF1();

            if (!Double.isNaN(localPrecision) && !Double.isNaN(localRecall) && !Double.isNaN(localF1)) {
                precision += localPrecision;
                recall += localRecall;
                f1 += localF1;
                counter++;
            }

            if (result instanceof ExtendedEvaluationResults extendedResult) {
                phi += extendedResult.getPhiCoefficient();
                phiMax += extendedResult.getPhiCoefficientMax();
                phiOverPhiMax += extendedResult.getPhiOverPhiMax();
                accuracy += extendedResult.getAccuracy();
                specificity += extendedResult.getSpecificity();
            }
        }

        precision /= counter;
        recall /= counter;
        f1 /= counter;

        if (phi != 0.0 && accuracy > 0.0) {
            phi /= counter;
            phiMax /= counter;
            phiOverPhiMax /= counter;
            accuracy /= counter;
            specificity /= counter;
            return new ExtendedEvaluationResultsImpl(precision, recall, f1, accuracy, phi, phiMax, phiOverPhiMax, specificity);
        }
        return new EvaluationResultsImpl(precision, recall, f1);

    }

    int getWeight() {
        int weight = 0;
        for (var entry : resultsWithWeight) {
            weight += entry.getTwo();
        }
        return weight;
    }

}
