/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
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
    public static final String DIRECTORY_NAME = "ardoco_eval_id";

    private static final OverallResultsCalculator OVERALL_RESULTS_CALCULATOR = new OverallResultsCalculator();
    private static final OverallResultsCalculator OVERALL_RESULT_CALCULATOR_BASELINE = new OverallResultsCalculator();
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static boolean ranBaseline = false;
    private static Map<Project, ImmutableList<InconsistentSentence>> inconsistentSentencesPerProject = new HashMap<>();

    @AfterAll
    public static void afterAll() {
        var weightedResults = OVERALL_RESULTS_CALCULATOR.calculateWeightedAveragePRF1();
        var macroResults = OVERALL_RESULTS_CALCULATOR.calculateMacroAveragePRF1();

        if (logger.isInfoEnabled()) {
            var name = "Overall Weighted";
            TestUtil.logResults(logger, name, weightedResults);

            name = "Overall Macro";
            TestUtil.logResults(logger, name, weightedResults);

            if (ranBaseline) {
                name = "BASELINE Overall Weighted";
                var results = OVERALL_RESULT_CALCULATOR_BASELINE.calculateWeightedAveragePRF1();
                TestUtil.logResults(logger, name, results);

                name = "BASELINE Overall Macro";
                results = OVERALL_RESULT_CALCULATOR_BASELINE.calculateMacroAveragePRF1();
                TestUtil.logResults(logger, name, results);
            }
        }

        try {
            writeOutput(weightedResults, macroResults);
        } catch (IOException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    private static void writeOutput(EvaluationResults weightedResults, EvaluationResults macroResults) throws IOException {
        var evalDir = Path.of(OUTPUT).resolve(DIRECTORY_NAME);
        Files.createDirectories(evalDir);
        var outputFile = evalDir.resolve("base_results.md");

        var outputBuilder = new StringBuilder("# Inconsistency Detection").append(LINE_SEPARATOR);

        var resultString = TestUtil.createResultLogString("Overall Weighted", weightedResults);
        outputBuilder.append(resultString).append(LINE_SEPARATOR);
        resultString = TestUtil.createResultLogString("Overall Macro", macroResults);
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
    @EnumSource(value = Project.class)
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
    @EnumSource(value = Project.class)
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
            String name = project.name();
            TestUtil.logResultsWithExpected(logger, name, results, expectedResults);
        }
    }

    private void checkResults(EvaluationResults results, EvaluationResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(), "Precision " + results
                        .getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(), "Recall " + results
                        .getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(), "F1 " + results
                        .getF1() + " is below the expected minimum value " + expectedResults.getF1()));
    }

    private void writeOutResults(Project project, List<ExplicitEvaluationResults<String>> results, Map<ModelInstance, ArDoCoResult> runs) {
        var outputs = createOutput(project, results, runs);
        var outputBuilder = outputs.getOne();
        var detailedOutputBuilder = outputs.getTwo();

        Path outputPath = Path.of(OUTPUT);
        Path idEvalPath = outputPath.resolve(DIRECTORY_NAME);
        try {
            Files.createDirectories(outputPath);
            Files.createDirectories(idEvalPath);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        String projectFileName = "inconsistencies_" + project.name().toLowerCase() + ".txt";
        var filename = idEvalPath.resolve(projectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(filename, outputBuilder.toString());

        String detailedProjectFileName = "detailed_" + projectFileName;
        var detailedFilename = idEvalPath.resolve(detailedProjectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(detailedFilename, detailedOutputBuilder.toString());
    }

    private static Pair<StringBuilder, StringBuilder> createOutput(Project project, List<ExplicitEvaluationResults<String>> results,
            Map<ModelInstance, ArDoCoResult> runs) {
        StringBuilder outputBuilder = createStringBuilderWithHeader(project);
        var resultCalculatorStringBuilderPair = inspectResults(results, runs, outputBuilder);
        var resultCalculator = resultCalculatorStringBuilderPair.getOne();
        outputBuilder.append(getOverallResultsString(resultCalculator));
        var detailedOutputBuilder = resultCalculatorStringBuilderPair.getTwo();
        return Tuples.pair(outputBuilder, detailedOutputBuilder);
    }

    private static String getOverallResultsString(ResultCalculator resultCalculator) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("###").append(LINE_SEPARATOR);
        var weightedAveragePRF1 = resultCalculator.getWeightedAveragePRF1();
        var resultString = TestUtil.createResultLogString("### OVERALL RESULTS ###" + LINE_SEPARATOR + "Weighted Average", weightedAveragePRF1);
        outputBuilder.append(resultString);
        outputBuilder.append(LINE_SEPARATOR);
        return outputBuilder.toString();
    }

    private static StringBuilder createStringBuilderWithHeader(Project project) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("### ").append(project.name()).append(" ###");
        outputBuilder.append(LINE_SEPARATOR);
        return outputBuilder;
    }

    private static Pair<ResultCalculator, StringBuilder> inspectResults(List<ExplicitEvaluationResults<String>> results, Map<ModelInstance, ArDoCoResult> runs,
            StringBuilder outputBuilder) {
        var detailedOutputBuilder = new StringBuilder();
        var resultCalculator = new ResultCalculator();
        int counter = 0;
        for (var run : runs.entrySet()) {
            ArDoCoResult arDoCoResult = run.getValue();
            ModelInstance instance = run.getKey();
            if (instance == null) {
                inspectBaseCase(outputBuilder, arDoCoResult);
            } else {
                outputBuilder.append("###").append(LINE_SEPARATOR);
                detailedOutputBuilder.append("###").append(LINE_SEPARATOR);
                outputBuilder.append("Removed Instance: ").append(instance.getFullName());
                detailedOutputBuilder.append("Removed Instance: ").append(instance.getFullName());
                outputBuilder.append(LINE_SEPARATOR);
                detailedOutputBuilder.append(LINE_SEPARATOR);
                var result = results.get(counter++);
                var resultString = String.format(Locale.ENGLISH, "Precision: %.3f, Recall: %.3f, F1: %.3f", result.getPrecision(), result.getRecall(), result
                        .getF1());
                outputBuilder.append(resultString);
                detailedOutputBuilder.append(resultString);
                inspectRun(outputBuilder, detailedOutputBuilder, resultCalculator, arDoCoResult, result);
            }

            outputBuilder.append(LINE_SEPARATOR);
        }

        return Tuples.pair(resultCalculator, detailedOutputBuilder);
    }

    private static void inspectRun(StringBuilder outputBuilder, StringBuilder detailedOutputBuilder, ResultCalculator resultCalculator,
            ArDoCoResult arDoCoResult, ExplicitEvaluationResults<String> result) {
        var truePositives = result.getTruePositives();
        appendResults(truePositives, detailedOutputBuilder, "True Positives", arDoCoResult, outputBuilder);

        var falsePositives = result.getFalsePositives();
        appendResults(falsePositives, detailedOutputBuilder, "False Positives", arDoCoResult, outputBuilder);

        var falseNegatives = result.getFalseNegatives();
        appendResults(falseNegatives, detailedOutputBuilder, "False Negatives", arDoCoResult, outputBuilder);

        resultCalculator.addEvaluationResults(truePositives.size(), falsePositives.size(), falseNegatives.size());
    }

    private static void appendResults(List<String> truePositives, StringBuilder detailedOutputBuilder, String type, ArDoCoResult arDoCoResult,
            StringBuilder outputBuilder) {
        truePositives = sortIntegerStrings(truePositives);
        detailedOutputBuilder.append(LINE_SEPARATOR).append("== ").append(type).append(" ==");
        detailedOutputBuilder.append(LINE_SEPARATOR).append(createDetailedOutputString(arDoCoResult, truePositives));
        outputBuilder.append(LINE_SEPARATOR).append(type).append(": ").append(listToString(truePositives));
    }

    private static void inspectBaseCase(StringBuilder outputBuilder, ArDoCoResult data) {
        var initialInconsistencies = getInitialInconsistencies(data);
        outputBuilder.append("Initial Inconsistencies: ").append(initialInconsistencies.size());
        var initialInconsistenciesSentences = initialInconsistencies.collect(MissingModelInstanceInconsistency::sentence)
                .toSortedSet()
                .collect(i -> i.toString());
        outputBuilder.append(LINE_SEPARATOR).append(listToString(initialInconsistenciesSentences));
    }

    private static String createDetailedOutputString(ArDoCoResult result, List<String> sentenceNumbers) {
        var outputBuilder = new StringBuilder();

        if (sentenceNumbers.isEmpty()) {
            return outputBuilder.append("None").append(LINE_SEPARATOR).toString();
        }

        for (var inconsistentSentence : result.getInconsistentSentences()) {
            int sentenceNumber = inconsistentSentence.sentence().getSentenceNumberForOutput();
            var sentenceNumberString = Integer.toString(sentenceNumber);
            if (sentenceNumbers.contains(sentenceNumberString)) {
                outputBuilder.append(inconsistentSentence.getInfoString());
                outputBuilder.append(LINE_SEPARATOR);
            }
        }

        return outputBuilder.toString();
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
