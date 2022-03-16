/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

public class PRF1Evaluator {

    private int tp;
    private int fp;
    private int fn;
    private List<PRF1> results;

    public PRF1Evaluator() {
        reset();
    }

    public PRF1 nextEvaluation(int tp, int fp, int fn) {
        this.tp += tp;
        this.fp += fp;
        this.fn += fn;

        var prf1 = new PRF1(tp, fp, fn);
        results.add(prf1);
        return prf1;
    }

    /**
     * Returns the weighted average EvaluationResults (Precision, Recall, F1 as {@link PRF1}. Weighted with number of
     * occurences.
     *
     * @return the weighted EvaluationResults (Precision, Recall, F1 as {@link PRF1}
     */
    public PRF1 getWeightedAveragePRF1() {
        return new PRF1(tp, fp, fn);
    }

    /**
     * Returns the average EvaluationResults (Precision, Recall, F1 as {@link PRF1}.
     *
     * @return the average EvaluationResults (Precision, Recall, F1 as {@link PRF1}
     */
    public PRF1 getAveragePRF1() {
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

        return new PRF1(avgPrecision, avgRecall, avgF1);
    }

    /**
     * Resets the evaluator, so previously committed results are removed.
     */
    public void reset() {
        tp = 0;
        fp = 0;
        fn = 0;
        this.results = Lists.mutable.empty();
    }
}
