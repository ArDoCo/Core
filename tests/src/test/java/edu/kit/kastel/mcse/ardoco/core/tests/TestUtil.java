/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.*;
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
     * Calculates the number of true negatives based on the given {@link ArDoCoResult} and the calculated {@link ExplicitEvaluationResults evaluation
     * results}.
     * Uses the total sum of all entries in the confusion matrix and then substracts the true positives, false positives, and false negatives.
     * 
     * @param arDoCoResult      the output of ArDoCo
     * @param evaluationResults the evaluation results
     * @return the number of true negatives
     */
    public static int calculateTrueNegativesForTLR(ArDoCoResult arDoCoResult, ExplicitEvaluationResults<?> evaluationResults) {
        var truePositives = evaluationResults.getTruePositives().size();
        var falsePositives = evaluationResults.getFalsePositives().size();
        var falseNegatives = evaluationResults.getFalseNegatives().size();

        int sentences = arDoCoResult.getText().getSentences().size();
        int modelElements = 0;
        for (var model : arDoCoResult.getModelIds()) {
            modelElements += arDoCoResult.getModelState(model).getInstances().size();
        }

        int confusionMatrixSum = sentences * modelElements;
        return confusionMatrixSum - (truePositives + falsePositives + falseNegatives);
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

        var sumInDenominator = a.multiply(b).multiply(c).multiply(d);
        var denominator = sumInDenominator.sqrt(MathContext.DECIMAL128);

        return num.divide(denominator, MathContext.DECIMAL128).doubleValue();
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
        return ((double) trueNegatives) / ((double) trueNegatives + falsePositives);
    }

    /**
     * Log the provided {@link EvaluationResultsImpl} using the provided logger and name.
     * 
     * @param logger  Logger to use
     * @param name    Name to show in the output
     * @param results the results
     */
    public static void logResults(Logger logger, String name, EvaluationResults results) {
        logger.info(createResultLogString(name, results));
    }

    /**
     * Creates a string from the given results that can be used, e.g., for logging. Extracts the name as well as precision, recall and F1-score and displays
     * them line by line.
     * 
     * @param name    the name that should be displayed
     * @param results the results
     * @return a String containing the name and the results (precision, recall, F1) line by line
     */
    public static String createResultLogString(String name, EvaluationResults results) {
        return String.format(Locale.ENGLISH, "%n%s:%n%s", name, results.getResultString());
    }

    /**
     * Log the provided {@link EvaluationResultsImpl} using the provided logger and name. Additionally, provided the
     * expected results.
     * 
     * @param logger          Logger to use
     * @param name            Name to show in the output
     * @param results         the results
     * @param expectedResults the expected results
     */
    public static void logResultsWithExpected(Logger logger, String name, EvaluationResults results, ExpectedResults expectedResults) {
        var infoString = String.format(Locale.ENGLISH, "%n%s:%n%s", name, results.getResultStringWithExpected(expectedResults));
        logger.info(infoString);
    }

    public static OverallResultsCalculator getOverallResultsCalculator(Map<Project, ExtendedExplicitEvaluationResults<?>> results) {

        var overallResultsCalculator = new OverallResultsCalculator();
        for (var entry : results.entrySet()) {
            var result = entry.getValue();
            var project = entry.getKey();

            ResultCalculator resultCalculator = new ResultCalculator();
            resultCalculator.addEvaluationResults(new ExtendedEvaluationResultsImpl(result.getPrecision(), result.getRecall(), result.getF1(), result
                    .getAccuracy(), result.getPhiCoefficient(), result.getSpecificity()), result.getWeight());
            overallResultsCalculator.addResult(project, resultCalculator);
        }
        return overallResultsCalculator;
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
            var weight = truePositives + falseNegatives;

            ResultCalculator resultCalculator = new ResultCalculator();
            resultCalculator.addEvaluationResults(new EvaluationResultsImpl(truePositives, falsePositives, falseNegatives), weight);
            overallResultsCalculator.addResult(result.getProject(), resultCalculator);
        }
        return overallResultsCalculator;
    }
}
