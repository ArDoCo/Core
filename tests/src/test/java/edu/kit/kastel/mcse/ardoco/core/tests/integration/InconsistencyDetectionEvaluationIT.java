/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExplicitEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ResultCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;

/**
 * Integration test that evaluates the inconsistency detection capabilities of ArDoCo. Runs on the projects that are
 * defined in the enum {@link Project}.
 *
 * Currently, the focus lies on detecting elements that are mentioned in the text but are not represented in the model.
 * For this, we run an evaluation that holds back (removes) one element from the model. This way, we know that there is
 * a missing element and the trace links to this element (in the gold standard) are the spots of inconsistency then. We
 * run this multiple times so each element was held back once.
 *
 */
class InconsistencyDetectionEvaluationIT {
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyDetectionEvaluationIT.class);
    private static final String OUTPUT = "src/test/resources/testout";

    /**
     * Tests the inconsistency detection on all {@link Project projects}.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @DisplayName("Evaluate Inconsistency Analyses")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(Project.class)
    void inconsistencyIT(Project project) {
        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ModelInstance, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, false);

        var results = calculateEvaluationResults(project, runs);
        ResultCalculator resultCalculator = results.getOne();
        var weightedResults = resultCalculator.getWeightedAveragePRF1();

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, weightedResults, expectedInconsistencyResults);
        checkResults(weightedResults, expectedInconsistencyResults);
        writeOutResults(project, results.getTwo(), runs);
    }

    /**
     * Tests the baseline approach that reports an inconsistency for each sentence that is not traced to a model
     * element. This test is enabled by providing the environment variable "testBaseline" with any value.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluate Inconsistency Analyses Baseline")
    @ParameterizedTest(name = "Evaluating Baseline For {0}")
    @EnumSource(Project.class)
    void inconsistencyBaselineIT(Project project) {
        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ModelInstance, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, true);

        Assertions.assertTrue(runs != null && runs.size() > 0);

        var results = calculateEvaluationResults(project, runs);
        ResultCalculator resultCalculator = results.getOne();
        var weightedResults = resultCalculator.getWeightedAveragePRF1();

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, weightedResults, expectedInconsistencyResults);
    }

    private Pair<ResultCalculator, List<ExplicitEvaluationResults<String>>> calculateEvaluationResults(Project project, Map<ModelInstance, ArDoCoResult> runs) {
        List<ExplicitEvaluationResults<String>> explicitResults = new ArrayList<>();
        ResultCalculator resultCalculator = new ResultCalculator();
        for (var run : runs.entrySet()) {
            var runEvalResults = evaluateRun(project, run.getKey(), run.getValue());
            if (runEvalResults != null) {
                int fn = runEvalResults.getFalseNegative().size();
                int fp = runEvalResults.getFalsePositives().size();
                int tp = runEvalResults.getTruePositives().size();
                resultCalculator.addEvaluationResults(tp, fp, fn);
                explicitResults.add(runEvalResults);
            }
        }
        return Tuples.pair(resultCalculator, explicitResults);
    }

    private ExplicitEvaluationResults<String> evaluateRun(Project project, ModelInstance removedElement, ArDoCoResult arDoCoResult) {
        var modelId = arDoCoResult.getModelIds().get(0);

        ImmutableList<MissingModelInstanceInconsistency> inconsistencies = arDoCoResult.getInconsistenciesOfTypeForModel(modelId,
                MissingModelInstanceInconsistency.class);
        if (removedElement == null) {
            // base case
            return null;
        }

        var goldStandard = project.getGoldStandard(getPcmModel(project));
        var expectedLines = goldStandard.getSentencesWithElement(removedElement).distinct().collect(i -> i.toString()).castToCollection();
        var actualSentences = inconsistencies.collect(MissingModelInstanceInconsistency::sentence).distinct().collect(i -> i.toString()).castToCollection();

        return TestUtil.compare(actualSentences, expectedLines);
    }

    private static PcmXMLModelConnector getPcmModel(Project project) {
        try {
            return new PcmXMLModelConnector(project.getModelFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logResults(Project project, EvaluationResults results, EvaluationResults expectedResults) {
        if (logger.isInfoEnabled()) {
            String infoString = String.format(Locale.ENGLISH,
                    "\n%s:\n\tPrecision:\t%.3f (min. expected: %.3f)%n\tRecall:\t\t%.3f (min. expected: %.3f)%n\tF1:\t\t%.3f (min. expected: %.3f)",
                    project.name(), results.getPrecision(), expectedResults.getPrecision(), results.getRecall(), expectedResults.getRecall(), results.getF1(),
                    expectedResults.getF1());
            logger.info(infoString);
        }
    }

    private void checkResults(EvaluationResults results, EvaluationResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(),
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(),
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(),
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.getF1()));
    }

    private void writeOutResults(Project project, List<ExplicitEvaluationResults<String>> results, Map<ModelInstance, ArDoCoResult> runs) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("### ").append(project.name()).append(" ###");
        outputBuilder.append(System.lineSeparator());

        int counter = 0;
        for (var run : runs.entrySet()) {
            var data = run.getValue();
            ModelInstance instance = run.getKey();
            if (instance == null) {
                var initialInconsistencies = getInitialInconsistencies(data);
                outputBuilder.append("Initial Inconsistencies: ").append(initialInconsistencies.size());
                var initialInconsistenciesSentences = initialInconsistencies.collect(MissingModelInstanceInconsistency::sentence)
                        .toSortedList()
                        .collect(i -> i.toString());
                outputBuilder.append(System.lineSeparator()).append(listToString(initialInconsistenciesSentences));
            } else {
                outputBuilder.append("###").append(System.lineSeparator());
                outputBuilder.append("Removed Instance: ").append(instance.getFullName());
                outputBuilder.append(System.lineSeparator());
                var result = results.get(counter++);
                var resultString = String.format(Locale.ENGLISH, "Precision: %.3f, Recall: %.3f, F1: %.3f", result.getPrecision(), result.getRecall(),
                        result.getF1());
                outputBuilder.append(resultString);
                var truePositives = result.getTruePositives();
                truePositives = sortIntegerStrings(truePositives);
                outputBuilder.append(System.lineSeparator()).append("True Positives: ").append(listToString(truePositives));
                var falsePositives = result.getFalsePositives();
                falsePositives = sortIntegerStrings(falsePositives);
                outputBuilder.append(System.lineSeparator()).append("False Positives: ").append(listToString(falsePositives));
                var falseNegatives = result.getFalseNegative();
                falseNegatives = sortIntegerStrings(falseNegatives);
                outputBuilder.append(System.lineSeparator()).append("False Negatives: ").append(listToString(falseNegatives));
            }

            outputBuilder.append(System.lineSeparator());
        }

        var filename = OUTPUT + "/inconsistencies_" + project.name().toLowerCase() + ".txt";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), UTF_8)) {
            writer.write(outputBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static List<String> sortIntegerStrings(List<String> list) {
        return list.stream().map(Integer::parseInt).sorted().map(i -> i.toString()).toList();
    }

    private static String listToString(List<?> truePositives) {
        return truePositives.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
    }

    private static ImmutableList<MissingModelInstanceInconsistency> getInitialInconsistencies(ArDoCoResult arDoCoResult) {
        var id = arDoCoResult.getModelIds().get(0);
        return arDoCoResult.getInconsistenciesOfTypeForModel(id, MissingModelInstanceInconsistency.class);
    }

}
