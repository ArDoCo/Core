/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval;

import java.math.BigDecimal;
import java.math.MathContext;

public class EvaluationMetrics {
    private EvaluationMetrics() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Checks the provided recall. Returns 1.0 if it is NaN, because this means that there was no missing
     * classification.
     *
     * @param recall the precision
     * @return 1.0 if recall is NaN, else the original value
     */
    public static double checkAndRepairRecall(double recall) {
        if (Double.isNaN(recall)) {
            return 1.0;
        }
        return recall;
    }

    /**
     * Calculates the recall for the given True Positives (TPs) and False Negatives (FNs). If TP+NP=0, then returns 1.0
     * because there was no missing element.
     *
     * @param truePositives  number of TPs
     * @param falseNegatives number of FNs
     * @return the Recall; 1.0 iff TP+NP=0
     */
    public static double calculateRecall(int truePositives, int falseNegatives) {
        double denominator = (truePositives + falseNegatives);
        var recall = 1.0 * truePositives / denominator;
        return checkAndRepairRecall(recall);
    }

    /**
     * Checks the provided precision. Returns 1.0 if it is NaN, because this means that there was no wrong
     * classification.
     *
     * @param precision the precision
     * @return 1.0 if precision is NaN, else the original value
     */
    public static double checkAndRepairPrecision(double precision) {
        if (Double.isNaN(precision)) {
            return 1.0;
        }
        return precision;
    }

    /**
     * Calculates the precision for the given True Positives (TPs) and False Positives (FPs). If TP+FP=0, then returns
     * 1.0 because there was no wrong classification.
     *
     * @param truePositives  number of TPs
     * @param falsePositives number of FPs
     * @return the Precision; 1.0 iff TP+FP=0
     */
    public static double calculatePrecision(int truePositives, int falsePositives) {
        double denominator = (truePositives + falsePositives);
        var precision = 1.0 * truePositives / denominator;
        return checkAndRepairPrecision(precision);
    }

    /**
     * Checks the provided F1-score. Iff it is NaN, returns 0.0, otherwise returns the original value
     *
     * @param f1 the f1-score to check
     * @return Iff score is NaN, returns 0.0, otherwise returns the original value
     */
    public static double checkAndRepairF1(double f1) {
        if (Double.isNaN(f1)) {
            return 0.0;
        }
        return f1;
    }

    /**
     * Calculates the F1-score using the provided precision and recall. If precision+recall=0, returns 0.0.
     *
     * @param precision the precision
     * @param recall    the recall
     * @return the F1-Score; 0.0 iff precision+recall=0
     */
    public static double calculateF1(double precision, double recall) {
        var f1 = 2 * precision * recall / (precision + recall);
        return checkAndRepairF1(f1);
    }

    /**
     * Calculates the F1-score using the provided True Positives (TPs), False Positives (FPs), and False Negatives
     * (FNs). If intermediate calculation shows that precision+recall=0, returns 0.0.
     *
     * @param truePositives  number of TPs
     * @param falsePositives number of FPs
     * @param falseNegatives number of FNs
     * @return the F1-score. See also {@link #calculateF1(double, double)}
     */
    public static double calculateF1(int truePositives, int falsePositives, int falseNegatives) {
        var precision = calculatePrecision(truePositives, falsePositives);
        var recall = calculateRecall(truePositives, falseNegatives);
        return calculateF1(precision, recall);
    }

    /**
     * Calculates the accuracy based on the true positives, false positives, false negatives, and true negatives.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Accuracy_and_precision">Wikipedia: Accuracy and Precision</a>
     * @return the accuracy
     */
    public static double calculateAccuracy(int truePositives, int falsePositives, int falseNegatives, int trueNegatives) {
        double numerator = truePositives + trueNegatives;
        double denominator = truePositives + falsePositives + falseNegatives + trueNegatives;
        return numerator / denominator;
    }

    /**
     * Returns the Phi Coefficient (also known as mean square contingency coefficient (MCC)) based on the true positives, false positives, false negatives, and
     * true negatives.
     * The return value lies between -1 and +1. -1 show perfect disagreement, +1 shows perfect agreement and 0 indicates no relationship.
     * Therefore, good values should be close to +1.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Phi_coefficient">Wikipedia: Phi coefficient</a>
     *
     * @return the value for Phi Coefficient (or MCC)
     */
    public static double calculatePhiCoefficient(int truePositives, int falsePositives, int falseNegatives, int trueNegatives) {
        var tp = BigDecimal.valueOf(truePositives);
        var fp = BigDecimal.valueOf(falsePositives);
        var fn = BigDecimal.valueOf(falseNegatives);
        var tn = BigDecimal.valueOf(trueNegatives);

        var num = tp.multiply(tn).subtract((fp.multiply(fn)));

        var a = tp.add(fp);
        var b = tp.add(fn);
        var c = tn.add(fp);
        var d = tn.add(fn);
        if (a.equals(BigDecimal.ZERO) || b.equals(BigDecimal.ZERO) || c.equals(BigDecimal.ZERO) || d.equals(BigDecimal.ZERO)) {
            return 0d;
        }

        var productOfSumsInDenominator = a.multiply(b).multiply(c).multiply(d);
        var denominator = productOfSumsInDenominator.sqrt(MathContext.DECIMAL128);

        return num.divide(denominator, MathContext.DECIMAL128).doubleValue();
    }

    /**
     * Calculates the maximum possible value of the phi coefficient given the four values of the confusion matrix (TP, FP, FN, TN).
     *
     * @see <a href="https://link.springer.com/article/10.1007/BF02288588">Paper about PhiMax by Ferguson (1941)</a>
     * @see <a href="https://journals.sagepub.com/doi/abs/10.1177/001316449105100403">Paper about Phi/PhiMax by Davenport et al. (1991)</a>
     * @param truePositives  number of true positives
     * @param falsePositives number of false positives
     * @param falseNegatives number of false negatives
     * @param trueNegatives  number of true negatives
     * @return The maximum possible value of phi.
     */
    public static double calculatePhiCoefficientMax(int truePositives, int falsePositives, int falseNegatives, int trueNegatives) {
        var tp = BigDecimal.valueOf(truePositives);
        var fp = BigDecimal.valueOf(falsePositives);
        var fn = BigDecimal.valueOf(falseNegatives);
        var tn = BigDecimal.valueOf(trueNegatives);

        var test = fn.add(tp).compareTo(fp.add(tp)) >= 0;
        var nominator = (fp.add(tn)).multiply(tp.add(fp)).sqrt(MathContext.DECIMAL128);
        var denominator = (fn.add(tn)).multiply(tp.add(fn)).sqrt(MathContext.DECIMAL128);
        if (test) {
            // standard case
            return nominator.divide(denominator, MathContext.DECIMAL128).doubleValue();
        } else {
            // if test is not true, you have to swap nominator and denominator as then you have to mirror the confusion matrix (,i.e., swap TP and TN)
            return denominator.divide(nominator, MathContext.DECIMAL128).doubleValue();
        }
    }

    /**
     * Calculates the normalized phi correlation coefficient value that is phi divided by its maximum possible value.
     * 
     * @see <a href="https://journals.sagepub.com/doi/abs/10.1177/001316449105100403">Paper about Phi/PhiMax</a>
     * @param truePositives  number of true positives
     * @param falsePositives number of false positives
     * @param falseNegatives number of false negatives
     * @param trueNegatives  number of true negatives
     * @return The value of Phi/PhiMax
     */
    public static double calculatePhiOverPhiMax(int truePositives, int falsePositives, int falseNegatives, int trueNegatives) {
        var phi = calculatePhiCoefficient(truePositives, falsePositives, falseNegatives, trueNegatives);
        var phiMax = calculatePhiCoefficientMax(truePositives, falsePositives, falseNegatives, trueNegatives);
        return phi / phiMax;
    }

    /**
     * Calculates the specificity, also known as selectivity or true negative rate, based on the number of true negatives and false positives.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Sensitivity_and_specificity">Wikipedia: Sensitivity and specificity</a>
     * @param trueNegatives  the number of true negatives
     * @param falsePositives the number of false positives
     * @return the specificity
     */
    public static double calculateSpecificity(int trueNegatives, int falsePositives) {
        double specificity = trueNegatives / ((double) trueNegatives + falsePositives);
        if (Double.isNaN(specificity)) {
            return 1.0;
        }
        return specificity;
    }
}
