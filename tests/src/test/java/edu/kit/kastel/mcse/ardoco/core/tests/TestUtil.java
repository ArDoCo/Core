/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExplicitEvaluationResults;

/**
 * This utility class provides methods for running the tests, especially regarding the evaluations.
 */
public class TestUtil {

    private TestUtil() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Compares a collection of results with the collection of the gold standard and returns an
     * {@link ExplicitEvaluationResults}.
     * 
     * @param results      Collection of Strings representing the results
     * @param goldStandard Collection of Strings representing the gold standard
     * @return the result of the comparison
     */
    public static ExplicitEvaluationResults<String> compare(Collection<String> results, Collection<String> goldStandard) {
        Set<String> distinctTraceLinks = new HashSet<>(results);
        Set<String> distinctGoldStandard = new HashSet<>(goldStandard);

        // True Positives are the trace links that are contained on both lists
        Set<String> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        List<String> truePositivesList = new ArrayList<>(truePositives);

        // False Positives are the trace links that are only contained in the result set
        Set<String> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        List<String> falsePositivesList = new ArrayList<>(falsePositives);

        // False Negatives are the trace links that are only contained in the gold standard
        Set<String> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        List<String> falseNegativesList = new ArrayList<>(falseNegatives);

        return new ExplicitEvaluationResults<>(truePositivesList, falseNegativesList, falsePositivesList);
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
    public static double calculateRecall(double truePositives, double falseNegatives) {
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
    public static double calculatePrecision(double truePositives, double falsePositives) {
        double denominator = (truePositives + falsePositives);
        var precision = 1.0 * truePositives / denominator;
        if (Double.isNaN(precision)) {
            precision = 1.0;
        }
        return precision;
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
        if (Double.isNaN(f1)) {
            f1 = 0.0;
        }
        return f1;
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
    public static double calculateF1(double truePositives, double falsePositives, double falseNegatives) {
        var precision = calculatePrecision(truePositives, falsePositives);
        var recall = calculateRecall(truePositives, falseNegatives);
        return calculateF1(precision, recall);
    }
}
