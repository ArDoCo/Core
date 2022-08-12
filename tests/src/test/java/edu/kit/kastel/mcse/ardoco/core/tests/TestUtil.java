/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExplicitEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.OverallResultsCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ResultCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;

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
    public static double calculateF1(double truePositives, double falsePositives, double falseNegatives) {
        var precision = calculatePrecision(truePositives, falsePositives);
        var recall = calculateRecall(truePositives, falseNegatives);
        return calculateF1(precision, recall);
    }

    /**
     * Log the provided {@link EvaluationResults} using the provided logger and name.
     * 
     * @param logger  Logger to use
     * @param name    Name to show in the output
     * @param results the results
     */
    public static void logResults(Logger logger, String name, EvaluationResults results) {
        String infoString = createResultLogString(name, results);
        logger.info(infoString);
    }

    public static String createResultLogString(String name, EvaluationResults results) {
        return String.format(Locale.ENGLISH, "%n%s:%n\tPrecision:%7.3f%n\tRecall:%10.3f%n\tF1:%14.3f", name, results.getPrecision(), results.getRecall(),
                results.getF1());
    }

    /**
     * Log the provided {@link EvaluationResults} using the provided logger and name. Additionally, provided the
     * expected results.
     * 
     * @param logger          Logger to use
     * @param name            Name to show in the output
     * @param results         the results
     * @param expectedResults the expected results
     */
    public static void logResultsWithExpected(Logger logger, String name, EvaluationResults results, EvaluationResults expectedResults) {
        var infoString = String.format(Locale.ENGLISH,
                "%n%s:%n\tPrecision:%7.3f (min. expected: %.3f)%n\tRecall:%10.3f (min. expected: %.3f)%n\tF1:%14.3f (min. expected: %.3f)", name, results
                        .getPrecision(), expectedResults.getPrecision(), results.getRecall(), expectedResults.getRecall(), results.getF1(), expectedResults
                                .getF1());
        logger.info(infoString);
    }

    /**
     * Constructs an {@link OverallResultsCalculator} using the provided list of {@link TLProjectEvalResult}s.
     * 
     * @param results the results to construct the {@link OverallResultsCalculator} from
     * @return an {@link OverallResultsCalculator} with the provided results
     */
    public static OverallResultsCalculator getOverallResultsCalculator(List<TLProjectEvalResult> results) {
        var overallResultsCalculator = new OverallResultsCalculator();
        for (var result : results) {
            var truePositives = result.getTruePositives().size();
            var falsePositives = result.getFalsePositives().size();
            var falseNegatives = result.getFalseNegatives().size();
            ResultCalculator resultCalculator = new ResultCalculator();
            resultCalculator.addEvaluationResults(truePositives, falsePositives, falseNegatives);
            overallResultsCalculator.addResult(result.getProject(), resultCalculator);
        }
        return overallResultsCalculator;
    }
}
