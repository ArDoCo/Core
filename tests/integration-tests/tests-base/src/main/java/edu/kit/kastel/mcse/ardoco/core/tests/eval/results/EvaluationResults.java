/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import java.util.Locale;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;

public record EvaluationResults<T>(double precision, double recall, double f1, ImmutableList<T> truePositives, int trueNegatives,
                                   ImmutableList<T> falseNegatives, ImmutableList<T> falsePositives, double accuracy, double phiCoefficient, double specificity,
                                   double phiCoefficientMax, double phiOverPhiMax) {

    public String toRow() {
        return String.format(Locale.ENGLISH, """
                %4s & %4s & %4s & %4s & %4s & %4s & %4s
                %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f""", "P", "R", "F1", "Acc", "Spec", "Phi", "PhiN", precision, recall, f1, accuracy,
                specificity, phiCoefficient, phiOverPhiMax);
    }

    public String toRow(String headerKey, String headerVal) {
        return String.format(Locale.ENGLISH, """
                %10s & %4s & %4s & %4s & %4s & %4s & %4s & %4s
                %10s & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f & %4.2f""", headerKey, "P", "R", "F1", "Acc", "Spec", "Phi", "PhiN", headerVal, precision,
                recall, f1, accuracy, specificity, phiCoefficient, phiOverPhiMax);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, """
                \tPrecision:%8.2f
                \tRecall:%11.2f
                \tF1:%15.2f
                \tAccuracy:%9.2f
                \tSpecificity:%6.2f
                \tPhi Coef.:%8.2f
                \tPhi/PhiMax:%7.2f (Phi Max: %.2f)
                %s""", precision, recall, f1, accuracy, specificity, phiCoefficient, phiOverPhiMax, phiCoefficientMax, toRow());
    }

    public String getResultStringWithExpected(ExpectedResults expectedResults) {
        return String.format(Locale.ENGLISH, """
                \tPrecision:%8.2f (min. expected: %.2f)
                \tRecall:%11.2f (min. expected: %.2f)
                \tF1:%15.2f (min. expected: %.2f)
                %s""", precision, expectedResults.precision(), recall, expectedResults.recall(), f1, expectedResults.f1(), toRow());
    }

    public String getExtendedResultStringWithExpected(ExpectedResults expectedResults) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append(String.format(Locale.ENGLISH, """
                \tPrecision:%8.2f (min. expected: %.2f)
                \tRecall:%11.2f (min. expected: %.2f)
                \tF1:%15.2f (min. expected: %.2f)""", precision, expectedResults.precision(), recall, expectedResults.recall(), f1, expectedResults.f1()));
        outputBuilder.append(String.format(Locale.ENGLISH, """

                \tAccuracy:%9.2f (min. expected: %.2f)
                \tSpecificity:%6.2f (min. expected: %.2f)""", accuracy, expectedResults.accuracy(), specificity, expectedResults.specificity()));
        outputBuilder.append(String.format(Locale.ENGLISH, """

                \tPhi Coef.:%8.2f (min. expected: %.2f)
                \tPhi/PhiMax:%7.2f (Phi Max: %.2f)
                %s""", phiCoefficient, expectedResults.phiCoefficient(), phiOverPhiMax, phiCoefficientMax, toRow()));
        return outputBuilder.toString();
    }

    public String getExplicitResultString() {
        return String.format(Locale.ENGLISH, """
                \tTP:%15d
                \tFP:%15d
                \tTN:%15d
                \tFN:%15d
                \tP:%16d
                \tN:%16d""", truePositives.size(), falsePositives.size(), trueNegatives, falseNegatives.size(), truePositives.size() + falseNegatives.size(),
                trueNegatives + falsePositives.size());
    }

    /**
     * returns the weight (truePos + falseNeg)
     *
     * @return the weight
     */
    public int getWeight() {
        return this.truePositives().size() + this.falseNegatives().size();
    }

    public ImmutableList<T> getFound() {
        MutableList<T> found = Lists.mutable.empty();
        found.addAll(truePositives.castToCollection());
        found.addAll(falsePositives.castToCollection());
        return found.toImmutable();
    }

    /**
     * creates new {@link EvaluationResults} from a {@link ResultMatrix}
     *
     * @param matrix the {@link ResultMatrix}
     * @return new {@link EvaluationResults}
     */
    public static <T> EvaluationResults<T> createEvaluationResults(ResultMatrix<T> matrix) {
        int nrTruePos = matrix.truePositives().size();
        int nrTrueNeg = matrix.trueNegatives();
        int nrFalsePos = matrix.falsePositives().size();
        int nrFalseNeg = matrix.falseNegatives().size();

        double precision = EvaluationMetrics.calculatePrecision(nrTruePos, nrFalsePos);
        double recall = EvaluationMetrics.calculateRecall(nrTruePos, nrFalseNeg);
        double f1 = EvaluationMetrics.calculateF1(precision, recall);

        double accuracy = 0;
        double phiCoefficient = 0;
        double specificity = 0;
        double phiCoefficientMax = 0;
        double phiOverPhiMax = 0;

        if (nrTruePos + nrFalsePos + nrFalseNeg + nrTrueNeg != 0) {
            accuracy = EvaluationMetrics.calculateAccuracy(nrTruePos, nrFalsePos, nrFalseNeg, nrTrueNeg);
        }
        phiCoefficient = EvaluationMetrics.calculatePhiCoefficient(nrTruePos, nrFalsePos, nrFalseNeg, nrTrueNeg);
        if (nrTrueNeg + nrFalsePos != 0) {
            specificity = EvaluationMetrics.calculateSpecificity(nrTrueNeg, nrFalsePos);
        }
        if ((nrFalseNeg + nrTrueNeg) * (nrTruePos + nrFalseNeg) != 0) {
            phiCoefficientMax = EvaluationMetrics.calculatePhiCoefficientMax(nrTruePos, nrFalsePos, nrFalseNeg, nrTrueNeg);
        }
        if (phiCoefficientMax != 0) {
            phiOverPhiMax = EvaluationMetrics.calculatePhiOverPhiMax(nrTruePos, nrFalsePos, nrFalseNeg, nrTrueNeg);
        }

        return new EvaluationResults<>(precision, recall, f1, matrix.truePositives(), matrix.trueNegatives(), matrix.falseNegatives(), matrix.falsePositives(),
                accuracy, phiCoefficient, specificity, phiCoefficientMax, phiOverPhiMax);
    }

}
