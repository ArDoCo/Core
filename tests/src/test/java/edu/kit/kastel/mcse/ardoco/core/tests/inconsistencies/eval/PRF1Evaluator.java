package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

public class PRF1Evaluator {

	private int tp;
	private int fp;
	private int fn;

	public PRF1Evaluator() {
		this.reset();
	}

	public PRF1 nextEvaluation(int tp, int fp, int fn) {
		this.tp += tp;
		this.fp += fp;
		this.fn += fn;

		return new PRF1(tp, fp, fn);
	}

	public PRF1 getOverallPRF1() {
		return new PRF1(this.tp, this.fp, this.fn);
	}

	public void reset() {
		this.tp = 0;
		this.fp = 0;
		this.fn = 0;
	}

	public static class PRF1 {
		public final double precision;
		public final double recall;
		public final double f1;

		PRF1(int tp, int fp, int fn) {
			this.precision = 1.0 * tp / (tp + fp);
			this.recall = 1.0 * tp / (tp + fn);
			this.f1 = 2 * this.precision * this.recall / (this.precision + this.recall);
		}

		@Override
		public String toString() {
			return String.format("P: %.2f, R: %.2f, F1: %.2f", this.precision, this.recall, this.f1);
		}

	}
}
