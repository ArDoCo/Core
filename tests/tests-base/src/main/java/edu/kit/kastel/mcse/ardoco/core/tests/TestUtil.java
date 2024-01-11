/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ResultMatrix;

/**
 * This utility class provides methods for running the tests, especially regarding the evaluations.
 */
public class TestUtil {

    private TestUtil() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * compares the tlr results with the expected results and creates a new {@link EvaluationResults}.
     *
     * @param arDoCoResult the ArDoCoResult
     * @param results      Collection representing the results
     * @param goldStandard Collection representing the gold standard
     * @return the result of the comparison
     */
    public static <T> EvaluationResults<T> compareTLR(ArDoCoResult arDoCoResult, ImmutableCollection<T> results, ImmutableCollection<T> goldStandard) {

        Set<T> distinctTraceLinks = new java.util.LinkedHashSet<>(results.castToCollection());
        Set<T> distinctGoldStandard = new java.util.LinkedHashSet<>(goldStandard.castToCollection());

        // True Positives are the trace links that are contained on both lists
        Set<T> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        ImmutableList<T> truePositivesList = Lists.immutable.ofAll(truePositives);

        // False Positives are the trace links that are only contained in the result set
        Set<T> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        ImmutableList<T> falsePositivesList = Lists.immutable.ofAll(falsePositives);

        // False Negatives are the trace links that are only contained in the gold standard
        Set<T> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        ImmutableList<T> falseNegativesList = Lists.immutable.ofAll(falseNegatives);

        int trueNegatives = TestUtil.calculateTrueNegativesForTLR(arDoCoResult, truePositives.size(), falsePositives.size(), falseNegatives.size());
        return EvaluationResults.createEvaluationResults(new ResultMatrix<>(truePositivesList, trueNegatives, falsePositivesList, falseNegativesList));
    }

    /**
     * compares the inconsistencies results with the expected results and creates a new {@link EvaluationResults}.
     *
     * @param arDoCoResult the ArDoCoResult
     * @param results      Collection representing the results
     * @param goldStandard Collection representing the gold standard
     * @return the result of the comparison
     */
    public static <T> EvaluationResults<T> compareInconsistencies(ArDoCoResult arDoCoResult, ImmutableCollection<T> results,
            ImmutableCollection<T> goldStandard) {

        Set<T> distinctTraceLinks = new java.util.LinkedHashSet<>(results.castToCollection());
        Set<T> distinctGoldStandard = new java.util.LinkedHashSet<>(goldStandard.castToCollection());

        // True Positives are the trace links that are contained on both lists
        Set<T> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        ImmutableList<T> truePositivesList = Lists.immutable.ofAll(truePositives);

        // False Positives are the trace links that are only contained in the result set
        Set<T> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        ImmutableList<T> falsePositivesList = Lists.immutable.ofAll(falsePositives);

        // False Negatives are the trace links that are only contained in the gold standard
        Set<T> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        ImmutableList<T> falseNegativesList = Lists.immutable.ofAll(falseNegatives);

        int trueNegatives = TestUtil.calculateTrueNegativesForInconsistencies(arDoCoResult, truePositives.size(), falsePositives.size(), falseNegatives.size());
        return EvaluationResults.createEvaluationResults(new ResultMatrix<>(truePositivesList, trueNegatives, falsePositivesList, falseNegativesList));
    }

    /**
     * Calculates the number of true negatives based on the given {@link ArDoCoResult} and the calculated {@link EvaluationResults evaluation results}. Uses the
     * total sum of all entries in the confusion matrix and then substracts the true positives, false positives, and false negatives.
     *
     * @param arDoCoResult   the output of ArDoCo
     * @param truePositives  nr of true positives
     * @param falsePositives nr of false positives
     * @param falseNegatives nr of false negatives
     * @return the number of true negatives
     */
    public static int calculateTrueNegativesForTLR(ArDoCoResult arDoCoResult, int truePositives, int falsePositives, int falseNegatives) {
        int sentences = arDoCoResult.getText().getSentences().size();
        int modelElements = 0;
        for (var model : arDoCoResult.getModelIds()) {
            modelElements += arDoCoResult.getModelState(model).getInstances().size();
        }

        int confusionMatrixSum = sentences * modelElements;
        return confusionMatrixSum - (truePositives + falsePositives + falseNegatives);
    }

    /**
     * Calculates the number of true negatives based on the given {@link ArDoCoResult} and the calculated {@link EvaluationResults evaluation results}. Uses the
     * total sum of all sentences in the {@link ArDoCoResult} and then substracts the true positives, false positives, and false negatives.
     *
     * @param arDoCoResult   the output of ArDoCo
     * @param truePositives  nr of true positives
     * @param falsePositives nr of false positives
     * @param falseNegatives nr of false negatives
     * @return the number of true negatives
     */
    public static int calculateTrueNegativesForInconsistencies(ArDoCoResult arDoCoResult, int truePositives, int falsePositives, int falseNegatives) {
        int numberOfSentences = arDoCoResult.getText().getSentences().size();
        return numberOfSentences - (truePositives + falsePositives + falseNegatives);

    }

    /**
     * Log the provided {@link EvaluationResults} using the provided logger and name. The log put out the result string provided by the
     * {@link EvaluationResults}.
     *
     * @param logger  Logger to use
     * @param name    Name to show in the output
     * @param results the results
     */
    public static void logResults(Logger logger, String name, EvaluationResults<?> results) {
        if (logger.isInfoEnabled())
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
    public static String createResultLogString(String name, EvaluationResults<?> results) {
        return String.format(Locale.ENGLISH, "%n%s:%n%s", name, results);
    }

    public static void logExtendedResultsAsRow(Logger logger, String headerVal, String headerKey, EvaluationResults<?> results) {
        var txt = String.format("%n%s", results.toRow(headerVal, headerKey));
        logger.info(txt);
    }

    /**
     * Log the provided {@link EvaluationResults} using the provided logger and name. Additionally, logs TP, FP, TN and FN used to calculate the metrics.
     *
     * @param logger  Logger to use
     * @param name    Name to show in the output
     * @param results the results
     */
    public static void logExplicitResults(Logger logger, String name, EvaluationResults<?> results) {
        var tp = results.truePositives().size();
        var fp = results.falsePositives().size();
        var fn = results.falseNegatives().size();
        var precisionDenominator = tp + fp;
        var recallDenominator = tp + fn;
        var logString = String.format(Locale.ENGLISH, "%n%s:%n\tPrecision:%7d/%d = %.3f%n\tRecall:%10d/%d = %.3f", name, tp, precisionDenominator, results
                .precision(), tp, recallDenominator, results.recall());
        logger.info(logString);
    }

    /**
     * Log the provided {@link EvaluationResults} using the provided logger and name. Additionally, provided the expected results.
     *
     * @param logger          Logger to use
     * @param name            Name to show in the output
     * @param results         the results
     * @param expectedResults the expected results
     */
    public static void logResultsWithExpected(Logger logger, String name, EvaluationResults<?> results, ExpectedResults expectedResults) {
        var infoString = String.format(Locale.ENGLISH, "%n%s:%n%s", name, results.getResultStringWithExpected(expectedResults));
        logger.info(infoString);
    }

    public static void logExtendedResultsWithExpected(Logger logger, Object testClass, String name, EvaluationResults<?> results,
            ExpectedResults expectedResults) {
        var infoString = String.format(Locale.ENGLISH, """

                %s (%s):
                %s""", name, testClass.getClass().getSimpleName(), results.getExtendedResultStringWithExpected(expectedResults));
        logger.info(infoString);
    }

}
