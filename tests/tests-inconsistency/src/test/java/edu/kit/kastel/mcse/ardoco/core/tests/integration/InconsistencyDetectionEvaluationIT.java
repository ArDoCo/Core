/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.PcmExtractor;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ResultMatrix;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator.ResultCalculatorUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;

/**
 * Integration test that evaluates the inconsistency detection capabilities of ArDoCo. Runs on the projects that are defined in the enum {@link Project}.
 * <p>
 * Currently, the focus lies on detecting elements that are mentioned in the text but are not represented in the model. For this, we run an evaluation that
 * holds back (removes) one element from the model. This way, we know that there is a missing element and the trace links to this element (in the gold standard)
 * are the spots of inconsistency then. We run this multiple times so each element was held back once.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InconsistencyDetectionEvaluationIT {
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyDetectionEvaluationIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    public static final String DIRECTORY_NAME = "ardoco_eval_id";

    /**
     * missing models in model
     */
    private static final MutableList<EvaluationResults<String>> OVERALL_MME_RESULTS = Lists.mutable.empty(); //
    private static final MutableList<EvaluationResults<String>> OVERALL_MME_RESULTS_BASELINE = Lists.mutable.empty();

    /**
     * undocumented models
     */
    private static final MutableList<EvaluationResults<String>> OVERALL_UME_RESULTS = Lists.mutable.empty();

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static boolean ranBaseline = false;
    private static final Map<Project, ImmutableList<InconsistentSentence>> inconsistentSentencesPerProject = new EnumMap<>(Project.class);
    private static final Map<Project, ArDoCoResult> arDoCoResults = new EnumMap<>(Project.class);

    /**
     * Tests the inconsistency detection for missing model elements on all {@link Project projects}.
     * <p>
     * NOTE: if you only want to test a specific project, you can simply set up the EnumSource. For more details, see
     * <a href="https://www.baeldung.com/parameterized-tests-junit-5#3-enum">here</a>
     * Example: add ", names = { "BIGBLUEBUTTON" }" to EnumSource However, make sure to revert this before you commit and push!
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @DisplayName("Evaluating MME-Inconsistency Detection")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(1)
    void missingModelElementInconsistencyIT(Project project) {
        runMissingModelElementInconsistencyEval(project);
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection (Historic)")
    @ParameterizedTest(name = "Evaluating MME-Inconsistency for {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_ALL, names = "^.*HISTORICAL$")
    @Order(2)
    void missingModelElementInconsistencyHistoricIT(Project project) {
        runMissingModelElementInconsistencyEval(project);
    }

    private void runMissingModelElementInconsistencyEval(Project project) {
        logger.info("Start evaluation of MME-inconsistency for {}", project.name());
        Map<ModelElement, ArDoCoResult> runs = produceRuns(project);

        var results = calculateEvaluationResults(project, runs);

        OVERALL_MME_RESULTS.addAll(results);

        EvaluationResults<String> weightedResults = ResultCalculatorUtil.calculateWeightedAverageResults(results.toImmutable());

        var expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResultsMissingModelInconsistency(project, weightedResults, expectedInconsistencyResults);
        checkResults(weightedResults, expectedInconsistencyResults);

        writeOutResults(project, results, runs);
    }

    /**
     * Tests the baseline approach that reports a missing model element inconsistency for each sentence that is not traced to a model element. This test is
     * enabled by providing the environment variable "testBaseline" with any value.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(5)
    void missingModelElementInconsistencyBaselineIT(Project project) {
        runMissingModelElementInconsistencyBaselineEval(project);
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluating MME-Inconsistency Detection Baseline (Historical)")
    @ParameterizedTest(name = "Evaluating Baseline for {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_ALL, names = "^.*HISTORICAL$")
    @Order(6)
    void missingModelElementInconsistencyBaselineHistoricIT(Project project) {
        runMissingModelElementInconsistencyBaselineEval(project);
    }

    private void runMissingModelElementInconsistencyBaselineEval(Project project) {
        logger.info("Start evaluation of MME-inconsistency baseline for {}", project.name());
        ranBaseline = true;

        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ModelElement, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, true);

        Assertions.assertTrue(runs != null && runs.size() > 0);

        var results = calculateEvaluationResults(project, runs);
        OVERALL_MME_RESULTS_BASELINE.addAll(results);

        if (logger.isInfoEnabled()) {
            var weightedResults = ResultCalculatorUtil.calculateWeightedAverageResults(results.toImmutable());
            String name = project.name() + " missing model inconsistency";
            TestUtil.logResults(logger, name, weightedResults);
        }
    }

    /**
     * Tests the inconsistency detection for undocumented model elements on all {@link Project projects}.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @DisplayName("Evaluate Inconsistency Analyses For MissingTextForModelElementInconsistencies")
    @ParameterizedTest(name = "Evaluating UME-inconsistency for {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(10)
    void missingTextInconsistencyIT(Project project) {
        runMissingTextInconsistencyEval(project);
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluate Inconsistency Analyses For MissingTextForModelElementInconsistencies (Historical)")
    @ParameterizedTest(name = "Evaluating UME-inconsistency for {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_ALL, names = "^.*HISTORICAL$")
    @Order(11)
    void missingTextInconsistencyHistoricIT(Project project) {
        runMissingTextInconsistencyEval(project);
    }

    private void runMissingTextInconsistencyEval(Project project) {
        var projectResults = arDoCoResults.get(project);
        if (projectResults == null) {
            produceRuns(project);
            projectResults = arDoCoResults.get(project);
        }
        Assertions.assertNotNull(projectResults, "No results found.");

        MutableList<String> expectedInconsistentModelElements = project.getMissingTextForModelElementGoldStandard();
        var inconsistentModelElements = projectResults.getAllModelInconsistencies().collect(ModelInconsistency::getModelInstanceUid).toList();
        var results = TestUtil.compareInconsistencies(projectResults, inconsistentModelElements.toImmutable(), expectedInconsistentModelElements.toImmutable());

        OVERALL_UME_RESULTS.add(results);

        String name = project.name() + " missing text inconsistency";
        TestUtil.logExplicitResults(logger, name, results);
        writeOutResults(project, results);
    }

    private static Map<ModelElement, ArDoCoResult> produceRuns(Project project) {
        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();

        Map<ModelElement, ArDoCoResult> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, false);

        ArDoCoResult baseArDoCoResult = runs.get(null);
        saveOutput(project, baseArDoCoResult);
        arDoCoResults.put(project, baseArDoCoResult);
        return runs;
    }

    @EnabledIfEnvironmentVariable(named = "overallResults", matches = ".*")
    @Test
    @Order(999)
    void overAllResultsIT() {
        var weightedResults = ResultCalculatorUtil.calculateWeightedAverageResults(OVERALL_MME_RESULTS.toImmutable());
        var macroResults = ResultCalculatorUtil.calculateWeightedAverageResults(OVERALL_MME_RESULTS.toImmutable());

        Assertions.assertNotNull(weightedResults);
        Assertions.assertNotNull(macroResults);

        var weightedUMEResults = ResultCalculatorUtil.calculateWeightedAverageResults(OVERALL_UME_RESULTS.toImmutable());
        var macroUMEResults = ResultCalculatorUtil.calculateAverageResults(OVERALL_UME_RESULTS.toImmutable());

        Assertions.assertNotNull(weightedUMEResults);
        Assertions.assertNotNull(macroUMEResults);

        if (logger.isInfoEnabled()) {
            var name = "MME Overall Weighted";
            TestUtil.logResults(logger, name, weightedResults);

            name = "MME Overall Macro";
            TestUtil.logResults(logger, name, macroResults);

            if (ranBaseline) {
                name = "MME BASELINE Overall Weighted";
                var results = ResultCalculatorUtil.calculateWeightedAverageResults(OVERALL_MME_RESULTS_BASELINE.toImmutable());
                TestUtil.logResults(logger, name, results);

                name = "MME BASELINE Overall Macro";
                results = ResultCalculatorUtil.calculateAverageResults(OVERALL_MME_RESULTS_BASELINE.toImmutable());
                TestUtil.logResults(logger, name, results);
            }

            name = "Undoc. Model Element Overall Weighted";
            TestUtil.logResults(logger, name, weightedUMEResults);
            name = "Undoc. Model Element Overall Macro";
            TestUtil.logResults(logger, name, macroUMEResults);
        }

        try {
            writeOutput(weightedResults, macroResults);
            writeOverallOutputMissingTextInconsistency(weightedUMEResults, macroUMEResults);
        } catch (IOException e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    private MutableList<EvaluationResults<String>> calculateEvaluationResults(Project project, Map<ModelElement, ArDoCoResult> runs) {

        MutableList<EvaluationResults<String>> results = Lists.mutable.empty();

        for (var run : runs.entrySet()) {
            ModelElement modelInstance = run.getKey();
            ArDoCoResult arDoCoResult = run.getValue();
            var runEvalResults = evaluateRun(project, modelInstance, arDoCoResult);
            if (runEvalResults != null) {
                results.add(runEvalResults);
            } else {
                // for the base case, instead of calculating results, save the found inconsistencies.
                inconsistentSentencesPerProject.put(project, arDoCoResult.getInconsistentSentences());
            }
        }

        return results;
    }

    private EvaluationResults<String> evaluateRun(Project project, ModelElement removedElement, ArDoCoResult arDoCoResult) {
        var modelId = arDoCoResult.getModelIds().get(0);

        ImmutableList<MissingModelInstanceInconsistency> inconsistencies = arDoCoResult.getInconsistenciesOfTypeForModel(modelId,
                MissingModelInstanceInconsistency.class);
        if (removedElement == null) {
            // base case
            return null;
        }

        var goldStandard = project.getTlrGoldStandard(getPcmModel(project));
        var expectedLines = goldStandard.getSentencesWithElement(removedElement).distinct().collect(Object::toString);
        var actualSentences = inconsistencies.collect(MissingModelInstanceInconsistency::sentence).distinct().collect(Object::toString);

        return calculateEvaluationResults(arDoCoResult, expectedLines, actualSentences);
    }

    private static EvaluationResults<String> calculateEvaluationResults(ArDoCoResult arDoCoResult, ImmutableList<String> expectedLines,
            ImmutableList<String> actualSentences) {
        return TestUtil.compareInconsistencies(arDoCoResult, actualSentences, expectedLines);
    }

    private static ArchitectureModel getPcmModel(Project project) {
        try {
            return new PcmExtractor(project.getModelFile().getAbsolutePath()).extractModel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void logResultsMissingModelInconsistency(Project project, EvaluationResults<String> weightedAverageResult, ExpectedResults expectedResults) {
        if (logger.isInfoEnabled()) {
            String name = project.name() + " missing model inconsistency";
            TestUtil.logResultsWithExpected(logger, name, weightedAverageResult, expectedResults);
        }
    }

    private void checkResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.precision() >= expectedResults.precision(), "Precision " + results
                        .precision() + " is below the expected minimum value " + expectedResults.precision()), //
                () -> Assertions.assertTrue(results.recall() >= expectedResults.recall(), "Recall " + results
                        .recall() + " is below the expected minimum value " + expectedResults.recall()), //
                () -> Assertions.assertTrue(results.f1() >= expectedResults.f1(), "F1 " + results
                        .f1() + " is below the expected minimum value " + expectedResults.f1()), () -> Assertions.assertTrue(results
                                .accuracy() >= expectedResults.accuracy(), "Accuracy " + results
                                        .accuracy() + " is below the expected minimum value " + expectedResults.accuracy()), //
                () -> Assertions.assertTrue(results.phiCoefficient() >= expectedResults.phiCoefficient(), "Phi coefficient " + results
                        .phiCoefficient() + " is below the expected minimum value " + expectedResults.phiCoefficient()));
    }

    private void writeOutResults(Project project, List<EvaluationResults<String>> results, Map<ModelElement, ArDoCoResult> runs) {
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

    private void writeOutResults(Project project, EvaluationResults<String> results) {
        Path outputPath = Path.of(OUTPUT);
        Path idEvalPath = outputPath.resolve(DIRECTORY_NAME);
        try {
            Files.createDirectories(outputPath);
            Files.createDirectories(idEvalPath);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }

        var outputBuilder = createStringBuilderWithHeader(project);
        outputBuilder.append(TestUtil.createResultLogString("Inconsistent Model Elements", results));
        outputBuilder.append(LINE_SEPARATOR);
        outputBuilder.append("Number of True Positives: ").append(results.truePositives().size());
        outputBuilder.append(LINE_SEPARATOR);
        outputBuilder.append("Number of False Positives: ").append(results.falsePositives().size());
        outputBuilder.append(LINE_SEPARATOR);
        outputBuilder.append("Number of False Negatives: ").append(results.falseNegatives().size());

        String projectFileName = "inconsistentModelElements_" + project.name().toLowerCase() + ".txt";
        var filename = idEvalPath.resolve(projectFileName).toFile().getAbsolutePath();
        FilePrinter.writeToFile(filename, outputBuilder.toString());
    }

    private static void saveOutput(Project project, ArDoCoResult arDoCoResult) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(arDoCoResult);

        String projectName = project.name().toLowerCase();
        var outputDir = Path.of(OUTPUT);
        var filename = projectName + ".txt";

        var outputFileTLR = outputDir.resolve("traceLinks_" + filename).toFile();
        FilePrinter.writeTraceabilityLinkRecoveryOutput(outputFileTLR, arDoCoResult);
        var outputFileID = outputDir.resolve("inconsistencyDetection_" + filename).toFile();
        FilePrinter.writeInconsistencyOutput(outputFileID, arDoCoResult);
    }

    private static Pair<StringBuilder, StringBuilder> createOutput(Project project, List<EvaluationResults<String>> results,
            Map<ModelElement, ArDoCoResult> runs) {
        StringBuilder outputBuilder = createStringBuilderWithHeader(project);
        var resultCalculatorStringBuilderPair = inspectResults(results, runs, outputBuilder);
        var resultCalculator = resultCalculatorStringBuilderPair.getOne();
        outputBuilder.append(getOverallResultsString(resultCalculator));
        var detailedOutputBuilder = resultCalculatorStringBuilderPair.getTwo();
        return Tuples.pair(outputBuilder, detailedOutputBuilder);
    }

    private static void writeOutput(EvaluationResults<String> weightedResults, EvaluationResults<String> macroResults) throws IOException {
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

    private static void writeOverallOutputMissingTextInconsistency(EvaluationResults<String> weightedResults, EvaluationResults<String> macroResults)
            throws IOException {
        var evalDir = Path.of(OUTPUT).resolve(DIRECTORY_NAME);
        Files.createDirectories(evalDir);
        var outputFile = evalDir.resolve("_MissingTextInconsistency_Overall_Results.md");

        var outputBuilder = new StringBuilder("# Inconsistency Detection - Missing Text For Model Element").append(LINE_SEPARATOR);

        var resultString = TestUtil.createResultLogString("Overall Weighted", weightedResults);
        outputBuilder.append(resultString).append(LINE_SEPARATOR);
        resultString = TestUtil.createResultLogString("Overall Macro", macroResults);
        outputBuilder.append(resultString).append(LINE_SEPARATOR);
        outputBuilder.append(LINE_SEPARATOR);
    }

    private static String getOverallResultsString(MutableList<EvaluationResults<String>> results) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append("###").append(LINE_SEPARATOR);
        var weightedAverageResults = ResultCalculatorUtil.calculateWeightedAverageResults(results.toImmutable());
        var resultString = TestUtil.createResultLogString("### OVERALL RESULTS ###" + LINE_SEPARATOR + "Weighted Average", weightedAverageResults);
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

    private static Pair<MutableList<EvaluationResults<String>>, StringBuilder> inspectResults(List<EvaluationResults<String>> results,
            Map<ModelElement, ArDoCoResult> runs, StringBuilder outputBuilder) {
        var detailedOutputBuilder = new StringBuilder();
        MutableList<EvaluationResults<String>> resultsWithWeight = Lists.mutable.empty();
        int counter = 0;
        for (var run : runs.entrySet()) {
            ArDoCoResult arDoCoResult = run.getValue();
            ArchitectureItem instance = (ArchitectureItem) run.getKey();
            if (instance == null) {
                inspectBaseCase(outputBuilder, arDoCoResult);
            } else {
                outputBuilder.append("###").append(LINE_SEPARATOR);
                detailedOutputBuilder.append("###").append(LINE_SEPARATOR);
                outputBuilder.append("Removed Instance: ").append(instance.getName());
                detailedOutputBuilder.append("Removed Instance: ").append(instance.getName());
                outputBuilder.append(LINE_SEPARATOR);
                detailedOutputBuilder.append(LINE_SEPARATOR);
                var result = results.get(counter++);
                var resultString = String.format(Locale.ENGLISH, "Precision: %.3f, Recall: %.3f, F1: %.3f, Accuracy: %.3f, Phi Coef.: %.3f", result.precision(),
                        result.recall(), result.f1(), result.accuracy(), result.phiCoefficient());
                outputBuilder.append(resultString);
                detailedOutputBuilder.append(resultString);
                inspectRun(outputBuilder, detailedOutputBuilder, resultsWithWeight, arDoCoResult, result);
            }

            outputBuilder.append(LINE_SEPARATOR);
        }

        return Tuples.pair(resultsWithWeight, detailedOutputBuilder);
    }

    private static void inspectRun(StringBuilder outputBuilder, StringBuilder detailedOutputBuilder, MutableList<EvaluationResults<String>> allResults,
            ArDoCoResult arDoCoResult, EvaluationResults<String> result) {
        var truePositives = result.truePositives().toList();
        appendResults(truePositives, detailedOutputBuilder, "True Positives", arDoCoResult, outputBuilder);

        var falsePositives = result.falsePositives().toList();
        appendResults(falsePositives, detailedOutputBuilder, "False Positives", arDoCoResult, outputBuilder);

        var falseNegatives = result.falseNegatives().toList();
        appendResults(falseNegatives, detailedOutputBuilder, "False Negatives", arDoCoResult, outputBuilder);

        var results = EvaluationResults.createEvaluationResults(new ResultMatrix<String>(truePositives.toImmutable(), 0, falsePositives.toImmutable(),
                falseNegatives.toImmutable()));
        allResults.add(results);
    }

    private static void appendResults(List<String> resultList, StringBuilder detailedOutputBuilder, String type, ArDoCoResult arDoCoResult,
            StringBuilder outputBuilder) {
        resultList = sortIntegerStrings(resultList);
        detailedOutputBuilder.append(LINE_SEPARATOR).append("== ").append(type).append(" ==");
        detailedOutputBuilder.append(LINE_SEPARATOR).append(createDetailedOutputString(arDoCoResult, resultList));
        outputBuilder.append(LINE_SEPARATOR).append(type).append(": ").append(listToString(resultList));
    }

    private static void inspectBaseCase(StringBuilder outputBuilder, ArDoCoResult data) {
        var initialInconsistencies = getInitialInconsistencies(data);
        outputBuilder.append("Initial Inconsistencies: ").append(initialInconsistencies.size());
        var initialInconsistenciesSentences = initialInconsistencies.collect(MissingModelInstanceInconsistency::sentence)
                .toSortedSet()
                .collect(Object::toString);
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
        return list.stream().map(Integer::parseInt).sorted().map(Object::toString).toList();
    }

    private static String listToString(List<?> truePositives) {
        return truePositives.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
    }

    private static ImmutableList<MissingModelInstanceInconsistency> getInitialInconsistencies(ArDoCoResult arDoCoResult) {
        var id = arDoCoResult.getModelIds().get(0);
        return arDoCoResult.getInconsistenciesOfTypeForModel(id, MissingModelInstanceInconsistency.class);
    }

}
