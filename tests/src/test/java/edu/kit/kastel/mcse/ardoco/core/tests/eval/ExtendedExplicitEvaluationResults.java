package edu.kit.kastel.mcse.ardoco.core.tests.eval;

/**
 * This class extends {@link ExplicitEvaluationResults} and therefore also {@link EvaluationResults} to be able to set a number of true negatives.
 * Setting true negatives allows calculating accuracy and phi coefficient (aka MCC).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Accuracy_and_precision">Wikipedia: Accuracy and Precision</a>,
 *      <a href="https://en.wikipedia.org/wiki/Phi_coefficient">Wikipedia: Phi coefficient</a>
 */
public class ExtendedExplicitEvaluationResults<T> extends ExplicitEvaluationResults<T> {

    private long trueNegatives = 0;

    /**
     * Create an instance of {@link ExtendedExplicitEvaluationResults} with the provided {@link ExplicitEvaluationResults} as base.
     * 
     * @param explicitEvaluationResults the underlying ExplicitEvaluationResults
     * @param trueNegatives             the number of true negatives
     */
    public ExtendedExplicitEvaluationResults(ExplicitEvaluationResults<T> explicitEvaluationResults, int trueNegatives) {
        super(explicitEvaluationResults.getTruePositives(), explicitEvaluationResults.getFalseNegatives(), explicitEvaluationResults.getFalsePositives());
        this.trueNegatives = trueNegatives;
    }

    /**
     * Returns the accuracy based on the true positives, false positives, false negatives, and true negatives in this result.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Accuracy_and_precision">Wikipedia: Accuracy and Precision</a>
     * @return the accuracy
     */
    public double getAccuracy() {
        var truePositives = this.getTruePositives().size();
        var falsePositives = this.getFalsePositives().size();
        var falseNegatives = this.getFalseNegatives().size();

        double numerator = truePositives + trueNegatives;
        double denominator = truePositives + falsePositives + falseNegatives + trueNegatives;
        return numerator / denominator;
    }

    /**
     * Returns the Phi Coefficient (also known as mean square contingency coefficient (MCC)) based on the true positives, false positives, false negatives, and
     * true negatives in the results.
     * The return value lies between -1 and +1. -1 show perfect disagreement, +1 shows perfect agreement and 0 indicates no relationship.
     * Therefore, good values should be close to +1.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Phi_coefficient">Wikipedia: Phi coefficient</a>
     * 
     * @return the value for Phi Coefficient (or MCC)
     */
    public double getPhiCoefficient() {
        long truePositives = this.getTruePositives().size();
        long falsePositives = this.getFalsePositives().size();
        long falseNegatives = this.getFalseNegatives().size();

        double numerator = (truePositives * trueNegatives) - (falsePositives * falseNegatives);

        long a = truePositives + falsePositives;
        long b = truePositives + falseNegatives;
        long c = trueNegatives + falsePositives;
        long d = trueNegatives + falseNegatives;
        if (a == 0 || b == 0 || c == 0 || d == 0) {
            return 0d;
        }
        long sumInDenominator = a * b * c * d;
        double denominator = Math.sqrt(sumInDenominator);

        return numerator / denominator;
    }

}
