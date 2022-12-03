/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.*;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.calculator.*;
import edu.kit.kastel.mcse.ardoco.core.tests_new.integration.tlrhelper.TLProjectEvalResult;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This utility class provides methods for running the tests, especially regarding the evaluations.
 */
public class TestUtil {

    private TestUtil() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Compares a collection of results with the collection of the gold standard and returns an
     * {@link EvaluationResults}.
     * 
     * @param results      Collection of Strings representing the results
     * @param goldStandard Collection of Strings representing the gold standard
     * @return the result of the comparison
     */
    public static EvaluationResults<String> compare(Collection<String> results, Collection<String> goldStandard) {
        Set<String> distinctTraceLinks = new HashSet<>(results);
        Set<String> distinctGoldStandard = new HashSet<>(goldStandard);

        // True Positives are the trace links that are contained on both lists
        Set<String> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        MutableList<String> truePositivesList = Lists.mutable.ofAll(truePositives);

        // False Positives are the trace links that are only contained in the result set
        Set<String> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        MutableList<String> falsePositivesList = Lists.mutable.ofAll(falsePositives);

        // False Negatives are the trace links that are only contained in the gold standard
        Set<String> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        MutableList<String> falseNegativesList = Lists.mutable.ofAll(falseNegatives);

        return EvaluationResults.createEvaluationResults(new ResultMatrix<>(truePositivesList.toImmutable(), 0,
                falsePositivesList.toImmutable(), falseNegativesList.toImmutable()));
    }

    /**
     * Calculates the number of true negatives based on the given {@link ArDoCoResult} and the calculated {@link EvaluationResults evaluation
     * results}.
     * Uses the total sum of all entries in the confusion matrix and then substracts the true positives, false positives, and false negatives.
     * 
     * @param arDoCoResult      the output of ArDoCo
     * @param evaluationResults the evaluation results
     * @return the number of true negatives
     */
    public static int calculateTrueNegativesForTLR(ArDoCoResult arDoCoResult, EvaluationResults<?> evaluationResults) {
        var truePositives = evaluationResults.truePositives().size();
        var falsePositives = evaluationResults.falsePositives().size();
        var falseNegatives = evaluationResults.falseNegatives().size();

        int sentences = arDoCoResult.getText().getSentences().size();
        int modelElements = 0;
        for (var model : arDoCoResult.getModelIds()) {
            modelElements += arDoCoResult.getModelState(model).getInstances().size();
        }

        int confusionMatrixSum = sentences * modelElements;
        return confusionMatrixSum - (truePositives + falsePositives + falseNegatives);
    }

    /**
     * Log the provided {@link EvaluationResults} using the provided logger and name. The log put out the result string provided by the
     * {@link EvaluationResults}.
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
        return String.format(Locale.ENGLISH, "%n%s:%n%s", name, results.toString());
    }

    /**
     * Log the provided {@link EvaluationResults} using the provided logger and name. The log contains Precision and Recall printed with explicit
     * numbers for the calculation.
     * See the following example output:
     * Precision: 4/4 = 1.000
     * Recall: 4/6 = 0.667
     *
     * @param logger  Logger to use
     * @param name    Name to show in the output
     * @param results the results
     */
    public static void logExplicitResults(Logger logger, String name, EvaluationResults results) {
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
     * Log the provided {@link EvaluationResults} using the provided logger and name. Additionally, provided the
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

    public static OverallResultsCalculator getOverallResultsCalculator(Map<Project, EvaluationResults<?>> results) {

        var overallResultsCalculator = new OverallResultsCalculator();
        for (var entry : results.entrySet()) {
            var result = entry.getValue();
            var project = entry.getKey();

            ResultCalculator resultCalculator = new ResultCalculator();
            resultCalculator.addEvaluationResults(new EvaluationResults(result.precision(), result.recall(), result.f1(),
                    Lists.immutable.empty(), 0, Lists.immutable.empty(), Lists.immutable.empty(),
                    result.accuracy(), result.phiCoefficient(), result.phiCoefficientMax(), result.phiOverPhiMax(), result.specificity()),
                    result.getWeight());
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
            var truePositives = result.getTruePositives().stream().toList();
            var falsePositives = result.getFalsePositives().stream().toList();
            var falseNegatives = result.getFalseNegatives().stream().toList();
            var weight = truePositives.size() + falseNegatives.size();

            ResultCalculator resultCalculator = new ResultCalculator();
            resultCalculator.addEvaluationResults(new EvaluationResults(0.0,0.0,0.0, Lists.immutable.ofAll(truePositives), 0,
                    Lists.immutable.ofAll(falsePositives),Lists.immutable.ofAll(falseNegatives),
                    0.0, 0.0, 0.0, 0.0, 0.0), weight);
            overallResultsCalculator.addResult(result.getProject(), resultCalculator);
        }
        return overallResultsCalculator;
    }
}
