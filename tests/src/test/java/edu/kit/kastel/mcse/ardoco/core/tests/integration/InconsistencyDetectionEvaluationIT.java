/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExplicitEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.OverallResultsCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ResultCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;

/**
 * Integration test that evaluates the inconsistency detection capabilities of ArDoCo. Runs on the projects that are
 * defined in the enum {@link Project}.
 * <p>
 * Currently, the focus lies on detecting elements that are mentioned in the text but are not represented in the model.
 * For this, we run an evaluation that holds back (removes) one element from the model. This way, we know that there is
 * a missing element and the trace links to this element (in the gold standard) are the spots of inconsistency then. We
 * run this multiple times so each element was held back once.
 */
class InconsistencyDetectionEvaluationIT {
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyDetectionEvaluationIT.class);
    private static final String OUTPUT = "src/test/resources/testout";

    private static final OverallResultsCalculator OVERALL_RESULTS_CALCULATOR = new OverallResultsCalculator();
    private static final OverallResultsCalculator OVERALL_RESULT_CALCULATOR_BASELINE = new OverallResultsCalculator();
    public static final String LINE_SEPARATOR = System.lineSeparator();
    private static boolean ranBaseline = false;
    private static Map<Project, ImmutableList<InconsistentSentence>> inconsistentSentencesPerProject = new HashMap<>();

    @AfterAll
    public static void afterAll() {
        var weightedResults = OVERALL_RESULTS_CALCULATOR.calculateWeightedAveragePRF1();
        var macroResults = OVERALL_RESULTS_CALCULATOR.calculateMacroAveragePRF1();
        var macroWeightedResults = OVERALL_RESULTS_CALCULATOR.calculateWeightedMacroAveragePRF1();

        if (logger.isInfoEnabled()) {
            var name = "Overall Weighted";
            var resultString = TestUtil.createResultLogString(name, weightedResults);
            logger.info(resultString);

            name = "Overall Macro";
            resultString = TestUtil.createResultLogString(name, macroResults);
            logger.info(resultString);

            name = "Overall Weighted Macro";
            resultString = TestUtil.createResultLogString(name, macroWeightedResults);
            logger.info(resultString);

            if (ranBaseline) {
                name = "BASELINE Overall Weighted";
                var results = OVERALL_RESULT_CALCULATOR_BASELINE.calculateWeightedAveragePRF1();
                TestUtil.logResults(logger, name, results);

                name = "BASELINE Overall Macro";
                results = OVERALL_RESULT_CALCULATOR_BASELINE.calculateMacroAveragePRF1();
                TestUtil.logResults(logger, name, results);

                name = "BASELINE Overall Weighted Macro";
                results = OVERALL_RESULT_CALCULATOR_BASELINE.calculateWeightedMacroAveragePRF1();
                TestUtil.logResults(logger, name, results);
            }
        }

        try {
            writeOutput(weightedResults, macroResults, macroWeightedResults);
        } catch (IOException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    private static void writeOutput(EvaluationResults weightedResults, EvaluationResults macroResults, EvaluationResults macroWeightedResults)
            throws IOException {
        var evalDir = Path.of(OUTPUT).resolve("id_eval");
        Files.createDirectories(evalDir);
        var outputFile = evalDir.resolve("base_results.md");

        var outputBuilder = new StringBuilder("# Inconsistency Detection").append(LINE_SEPARATOR);

        var resultString = TestUtil.createResultLogString("Overall Weighted", weightedResults);
        outputBuilder.append(resultString).append(LINE_SEPARATOR);
        resultString = TestUtil.createResultLogString("Overall Macro", macroResults);
        outputBuilder.append(resultString).append(LINE_SEPARATOR);
        resultString = TestUtil.createResultLogString("Overall Weighted Macro", macroWeightedResults);
        outputBuilder.append(resultString).append(LINE_SEPARATOR);
        outputBuilder.append(LINE_SEPARATOR);

        for (var entry : inconsistentSentencesPerProject.entrySet()) {
            var project = entry.getKey();
            outputBuilder.append("## ").append(project.name());
            outputBuilder.append(LINE_SEPARATOR);
            var inconsistentSentences = entry.getValue();
            for (var inconsistentSentence : inconsistentSentences) {
                outputBuilder.append(inconsistentSentence.getInfoString());
                outputBuilder.append(LINE_SEPARATOR);
            }
        }

        Files.writeString(outputFile, outputBuilder.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Tests the inconsistency detection on all {@link Project projects}.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @DisplayName("Evaluate Inconsistency Analyses")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(Project.class)
    void inconsistencyIT(Project project) {
        logger.info("Start evaluation of inconsistency for {}", project.name());
        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ModelInstance, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, false);

        var results = calculateEvaluationResults(project, runs);
        ResultCalculator resultCalculator = results.getOne();
        OVERALL_RESULTS_CALCULATOR.addResult(project, resultCalculator);
        var weightedResults = resultCalculator.getWeightedAveragePRF1();

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, resultCalculator, expectedInconsistencyResults);
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
        logger.info("Start evaluation of inconsistency baseline for {}", project.name());
        ranBaseline = true;

        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ModelInstance, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, true);

        Assertions.assertTrue(runs != null && runs.size() > 0);

        var results = calculateEvaluationResults(project, runs);
        ResultCalculator resultCalculator = results.getOne();
        OVERALL_RESULT_CALCULATOR_BASELINE.addResult(project, resultCalculator);

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, resultCalculator, expectedInconsistencyResults);
    }

    private Pair<ResultCalculator, List<ExplicitEvaluationResults<String>>> calculateEvaluationResults(Project project, Map<ModelInstance, ArDoCoResult> runs) {
        List<ExplicitEvaluationResults<String>> explicitResults = new ArrayList<>();
        ResultCalculator resultCalculator = new ResultCalculator();
        for (var run : runs.entrySet()) {
            ModelInstance modelInstance = run.getKey();
            ArDoCoResult arDoCoResult = run.getValue();
            var runEvalResults = evaluateRun(project, modelInstance, arDoCoResult);
            if (runEvalResults != null) {
                int fn = runEvalResults.getFalseNegatives().size();
                int fp = runEvalResults.getFalsePositives().size();
                int tp = runEvalResults.getTruePositives().size();
                resultCalculator.addEvaluationResults(tp, fp, fn);
                explicitResults.add(runEvalResults);
            } else {
                // for the base case, instead of calculating results, save the found inconsistencies.
                inconsistentSentencesPerProject.put(project, arDoCoResult.getInconsistentSentences());
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

    private void logResults(Project project, ResultCalculator resultCalculator, EvaluationResults expectedResults) {
        if (logger.isInfoEnabled()) {
            var results = resultCalculator.getWeightedAveragePRF1();
            String name = project.name() + " (weighted)";
            TestUtil.logResultsWithExpected(logger, name, results, expectedResults);

            results = resultCalculator.getMacroAveragePRF1();
            name = project.name() + " (macro)";
            TestUtil.logResults(logger, name, results);
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
        outputBuilder.append(LINE_SEPARATOR);

        int counter = 0;
        for (var run : runs.entrySet()) {
            var data = run.getValue();
            ModelInstance instance = run.getKey();
            if (instance == null) {
                var initialInconsistencies = getInitialInconsistencies(data);
                outputBuilder.append("Initial Inconsistencies: ").append(initialInconsistencies.size());
                var initialInconsistenciesSentences = initialInconsistencies.collect(MissingModelInstanceInconsistency::sentence)
                        .toSortedSet()
                        .collect(i -> i.toString());
                outputBuilder.append(LINE_SEPARATOR).append(listToString(initialInconsistenciesSentences));
            } else {
                outputBuilder.append("###").append(LINE_SEPARATOR);
                outputBuilder.append("Removed Instance: ").append(instance.getFullName());
                outputBuilder.append(LINE_SEPARATOR);
                var result = results.get(counter++);
                var resultString = String.format(Locale.ENGLISH, "Precision: %.3f, Recall: %.3f, F1: %.3f", result.getPrecision(), result.getRecall(),
                        result.getF1());
                outputBuilder.append(resultString);
                var truePositives = result.getTruePositives();
                truePositives = sortIntegerStrings(truePositives);
                outputBuilder.append(LINE_SEPARATOR).append("True Positives: ").append(listToString(truePositives));
                var falsePositives = result.getFalsePositives();
                falsePositives = sortIntegerStrings(falsePositives);
                outputBuilder.append(LINE_SEPARATOR).append("False Positives: ").append(listToString(falsePositives));
                var falseNegatives = result.getFalseNegatives();
                falseNegatives = sortIntegerStrings(falseNegatives);
                outputBuilder.append(LINE_SEPARATOR).append("False Negatives: ").append(listToString(falseNegatives));
            }

            outputBuilder.append(LINE_SEPARATOR);
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
