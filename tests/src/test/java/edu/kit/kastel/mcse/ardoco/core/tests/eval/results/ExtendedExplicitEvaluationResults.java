/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;

/**
 * This class extends {@link ExplicitEvaluationResults} and therefore also {@link EvaluationResultsImpl} and implements {@link ExtendedEvaluationResults} to be
 * able to set a number of true negatives.
 * Setting true negatives allows calculating accuracy and phi coefficient (aka MCC).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Accuracy_and_precision">Wikipedia: Accuracy and Precision</a>,
 *      <a href="https://en.wikipedia.org/wiki/Phi_coefficient">Wikipedia: Phi coefficient</a>
 */
public class ExtendedExplicitEvaluationResults<T> extends ExplicitEvaluationResults<T> implements ExtendedEvaluationResults {

    private int trueNegatives = 0;

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

    public int getWeight() {
        return this.getTruePositives().size() + this.getFalseNegatives().size();
    }

    public int getNumberOfTrueNegatives() {
        return trueNegatives;
    }

    @Override
    public double getAccuracy() {
        var truePositives = this.getTruePositives().size();
        var falsePositives = this.getFalsePositives().size();
        var falseNegatives = this.getFalseNegatives().size();

        return EvaluationMetrics.calculateAccuracy(truePositives, falsePositives, falseNegatives, trueNegatives);
    }

    @Override
    public double getSpecificity() {
        return EvaluationMetrics.calculateSpecificity(trueNegatives, this.getFalsePositives().size());
    }

    @Override
    public double getPhiCoefficient() {
        int truePositives = this.getTruePositives().size();
        int falsePositives = this.getFalsePositives().size();
        int falseNegatives = this.getFalseNegatives().size();

        return EvaluationMetrics.calculatePhiCoefficient(truePositives, falsePositives, falseNegatives, trueNegatives);
    }

    @Override
    public double getPhiCoefficientMax() {
        int truePositives = this.getTruePositives().size();
        int falsePositives = this.getFalsePositives().size();
        int falseNegatives = this.getFalseNegatives().size();

        return EvaluationMetrics.calculatePhiCoefficientMax(truePositives, falsePositives, falseNegatives, trueNegatives);
    }

    @Override
    public double getPhiOverPhiMax() {
        int truePositives = this.getTruePositives().size();
        int falsePositives = this.getFalsePositives().size();
        int falseNegatives = this.getFalseNegatives().size();

        return EvaluationMetrics.calculatePhiOverPhiMax(truePositives, falsePositives, falseNegatives, trueNegatives);
    }

}
