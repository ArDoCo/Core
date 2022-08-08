/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

public class ResultCalculator {

    private int truePositives;
    private int falsePositives;
    private int falseNegatives;
    private List<EvaluationResults> results;

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

        var evalResults = new EvaluationResults(truePositives, falsePositives, falseNegatives);
        results.add(evalResults);
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
        return new EvaluationResults(truePositives, falsePositives, falseNegatives);
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
        for (var prf1 : results) {
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
