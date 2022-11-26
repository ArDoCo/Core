package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Locale;

public record EvaluationResults<T> (double precision, double recall, double f1,
                                    ImmutableList<T> truePositives, ImmutableList<T> trueNegatives,
                                    ImmutableList<T> falseNegatives, ImmutableList<T> falsePositives,
                                    double accuracy, double phiCoefficient, double specificity,
                                    double phiCoefficientMax, double phiOverPhiMax) {

    @Override
    public String toString() {
        String output = String.format(Locale.ENGLISH, "\tPrecision:%8.2f%n\tRecall:%11.2f%n\tF1:%15.2f", precision, recall, f1);
        output += String.format(Locale.ENGLISH, "%n\tAccuracy:%9.2f%n\tSpecificity:%6.2f", accuracy, specificity);
        output += String.format(Locale.ENGLISH, "%n\tPhi Coef.:%8.2f%n\tPhi/PhiMax:%7.2f (Phi Max: %.2f)", phiCoefficient, phiOverPhiMax,
                phiCoefficientMax);
        return output;
    }

    public static <T> EvaluationResults<T> createEvaluationResults(ResultMatrix<T> matrix) {
        int nrTruePos = matrix.truePositives().size();
        int nrTrueNeg = matrix.trueNegatives().size();
        int nrFalsePos = matrix.falsePositives().size();
        int nrFalseNeg = matrix.falseNegatives().size();

        double precision = EvaluationMetrics.calculatePrecision(nrTruePos, nrFalseNeg);
        double recall = EvaluationMetrics.calculateRecall(nrTruePos, nrFalseNeg);
        double f1 = EvaluationMetrics.calculateF1(precision, recall);

        double accuracy = EvaluationMetrics.calculateAccuracy(nrTruePos, nrFalsePos,
                nrFalseNeg, nrTrueNeg);
        double phiCoefficient = EvaluationMetrics.calculatePhiCoefficient(nrTruePos, nrFalsePos,
                nrFalseNeg, nrTrueNeg);
        double specificity = EvaluationMetrics.calculateSpecificity(nrTrueNeg, nrFalsePos);
        double phiCoefficientMax = EvaluationMetrics.calculatePhiCoefficientMax(nrTruePos, nrFalsePos,
                nrFalseNeg, nrTrueNeg);
        double phiOverPhiMax = EvaluationMetrics.calculatePhiOverPhiMax(nrTruePos, nrFalsePos,
                nrFalseNeg, nrTrueNeg);

        return new EvaluationResults<T>(precision, recall, f1,
                matrix.truePositives(), matrix.trueNegatives(),
                matrix.falseNegatives(), matrix.falsePositives(),
                accuracy, phiCoefficient, specificity,
                phiCoefficientMax, phiOverPhiMax);
    }

}
