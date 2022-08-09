/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;

public class ResultCalculator {

    private int truePositives;
    private int falsePositives;
    private int falseNegatives;
    private MutableList<Pair<EvaluationResults, Integer>> results;

    public ResultCalculator() {
        reset();
    }

    /**
     * Adds a new evaluation result using the provided True Positives, False Positives, and False Negatives.
     * 
     * @param truePositives  the TPs
     * @param falsePositives the FPs
     * @param falseNegatives the FNs
     */
    public void addEvaluationResults(int truePositives, int falsePositives, int falseNegatives) {
        this.truePositives += truePositives;
        this.falsePositives += falsePositives;
        this.falseNegatives += falseNegatives;

        int weight = truePositives + falseNegatives;
        var precision = TestUtil.calculatePrecision(truePositives, falsePositives);
        var recall = TestUtil.calculateRecall(truePositives, falseNegatives);
        var f1 = TestUtil.calculateF1(precision, recall);

        var evalResults = new EvaluationResults(precision, recall, f1);
        results.add(Tuples.pair(evalResults, weight));
    }

    /**
     * Adds the results from another ResultCalculator to this one. To do so, grabs the True Positives, False Positives,
     * and False Negatives and calls {@link #addEvaluationResults(int, int, int)}, therefore treating the other
     * ResultCalculator as one result. There is no copying of the detailed results of the other ResultCalculator!
     * 
     * @param other the other ResultCalculator
     */
    public void addEvaluationResultsFromOtherResultCalculator(ResultCalculator other) {
        this.addEvaluationResults(other.truePositives, other.falsePositives, other.falseNegatives);
    }

    /**
     * Returns the weighted average EvaluationResults (Precision, Recall, F1 as {@link EvaluationResults}. Weighted with
     * number of occurrences.
     *
     * @return the weighted EvaluationResults (Precision, Recall, F1 as {@link EvaluationResults}
     */
    public EvaluationResults getWeightedAveragePRF1() {
        int weight = 0;
        double precision = 0.0;
        double recall = 0.0;
        double f1 = 0.0;

        for (var result : results) {
            var prf1 = result.getOne();
            int localWeight = result.getTwo();
            double localPrecision = prf1.getPrecision();
            double localRecall = prf1.getRecall();
            double localF1 = prf1.getF1();

            if (!Double.isNaN(localPrecision) && !Double.isNaN(localRecall) && !Double.isNaN(localF1)) {
                precision += (localWeight * localPrecision);
                recall += (localWeight * localRecall);
                f1 += (localWeight * localF1);
                weight += localWeight;
            }
        }

        precision = precision / weight;
        recall = recall / weight;
        f1 = f1 / weight;

        return new EvaluationResults(precision, recall, f1);
    }

    /**
     * Returns the average EvaluationResults (Precision, Recall, F1 as {@link EvaluationResults}.
     *
     * @return the average EvaluationResults (Precision, Recall, F1 as {@link EvaluationResults}
     */
    public EvaluationResults getMacroAveragePRF1() {
        var avgPrecision = 0.0;
        var avgRecall = 0.0;
        var avgF1 = 0.0;

        var counter = 0;
        for (var result : results) {
            var prf1 = result.getOne();
            var precision = prf1.getPrecision();
            var recall = prf1.getRecall();
            var f1 = prf1.getF1();

            if (!Double.isNaN(precision) && !Double.isNaN(recall) && !Double.isNaN(f1)) {
                avgPrecision += precision;
                avgRecall += recall;
                avgF1 += f1;
                counter++;
            }
        }

        avgPrecision /= counter;
        avgRecall /= counter;
        avgF1 /= counter;

        return new EvaluationResults(avgPrecision, avgRecall, avgF1);
    }

    /**
     * Resets the evaluator, so previously committed results are removed.
     */
    public void reset() {
        truePositives = 0;
        falsePositives = 0;
        falseNegatives = 0;
        this.results = Lists.mutable.empty();
    }

}
