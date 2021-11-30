/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

public class PRF1Evaluator {

    private int tp;
    private int fp;
    private int fn;

    public PRF1Evaluator() {
        reset();
    }

    public EvaluationResult nextEvaluation(int tp, int fp, int fn) {
        this.tp += tp;
        this.fp += fp;
        this.fn += fn;

        return new PRF1(tp, fp, fn);
    }

    public EvaluationResult getOverallPRF1() {
        return new PRF1(tp, fp, fn);
    }

    public void reset() {
        tp = 0;
        fp = 0;
        fn = 0;
    }
}
