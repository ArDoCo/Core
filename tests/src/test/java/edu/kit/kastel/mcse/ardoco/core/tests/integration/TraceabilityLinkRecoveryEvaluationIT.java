/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator.ResultCalculatorUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLRUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TestLink;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.*;

/**
 * Integration test that evaluates the traceability link recovery capabilities of ArDoCo. Runs on the projects that are
 * defined in the enum {@link Project}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TraceabilityLinkRecoveryEvaluationIT {
    private static final Logger logger = LoggerFactory.getLogger(TraceabilityLinkRecoveryEvaluationIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final Path OUTPUT_PATH = Path.of(OUTPUT);
    private static final String ADDITIONAL_CONFIG = null;
    private static final List<Pair<Project, EvaluationResults<TestLink>>> RESULTS = new ArrayList<>();
    private static final MutableList<EvaluationResults<String>> PROJECT_RESULTS = Lists.mutable.empty();
    private static final Map<Project, ArDoCoResult> DATA_MAP = new EnumMap<>(Project.class);
    private static final boolean detailedDebug = true;
    private static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    private String name;
    private File inputText;
    private File inputModel;
    private File inputCodeModel = null;
    private final File outputDir = new File(OUTPUT);

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    public static void afterAll() {
        if (logger.isInfoEnabled()) {
            var name = "Overall Weighted";
            var results = ResultCalculatorUtil.calculateWeightedAverageResults(PROJECT_RESULTS.toImmutable());
            TestUtil.logResults(logger, name, results);

            name = "Overall Macro";
            results = ResultCalculatorUtil.calculateAverageResults(PROJECT_RESULTS.toImmutable());
            TestUtil.logResults(logger, name, results);
        }

        if (detailedDebug) {
            var evalDir = Path.of(OUTPUT).resolve("ardoco_eval_tl");

            try {
                Files.createDirectories(evalDir);

                TLSummaryFile.save(evalDir.resolve("summary.txt"), RESULTS, DATA_MAP);
                TLModelFile.save(evalDir.resolve("models.txt"), DATA_MAP);
                TLSentenceFile.save(evalDir.resolve("sentences.txt"), DATA_MAP);
                TLLogFile.append(evalDir.resolve("log.txt"), RESULTS);
                TLPreviousFile.save(evalDir.resolve("previous.csv"), RESULTS, logger); // save before loading
                TLDiffFile.save(evalDir.resolve("diff.txt"), RESULTS, TLPreviousFile.load(evalDir.resolve("previous.csv"), DATA_MAP), DATA_MAP);
            } catch (IOException e) {
                logger.error("Failed to write output.", e);
            }
        }
        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    @AfterEach
    void afterEach() {
        if (ADDITIONAL_CONFIG != null) {
            var config = new File(ADDITIONAL_CONFIG);
            config.delete();
        }
    }

    @DisplayName("Evaluate TLR")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(1)
    void evaluateTraceLinkRecoveryIT(Project project) {
        runTraceLinkEvaluation(project);
    }

    @DisplayName("Evaluate TLR (Historical)")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = Project.class, mode = EnumSource.Mode.MATCH_ALL, names = "^.*HISTORICAL$")
    @Order(2)
    void evaluateHistoricalDataTraceLinkRecoveryIT(Project project) {
        runTraceLinkEvaluation(project);
    }

    private void runTraceLinkEvaluation(Project project) {
        ArDoCoResult arDoCoResult = getArDoCoResult(project);
        Assertions.assertNotNull(arDoCoResult);

        checkResults(project, arDoCoResult);

        writeDetailedOutput(project, arDoCoResult);
    }

    private ArDoCoResult getArDoCoResult(Project project) {
        name = project.name().toLowerCase();
        inputModel = project.getModelFile();
        inputText = project.getTextFile();

        var arDoCoResult = DATA_MAP.get(project);
        if (arDoCoResult == null) {
            File additionalConfigurations = project.getAdditionalConfigurationsFile();
            arDoCoResult = getArDoCoResult(name, inputText, inputModel, additionalConfigurations);
            DATA_MAP.put(project, arDoCoResult);
        }
        return arDoCoResult;
    }

    private ArDoCoResult getArDoCoResult(String name, File inputText, File inputModel, File additionalConfigurations) {
        ArDoCo arDoCo = ArDoCo.getInstance(name);
        return arDoCo.runAndSave(name, inputText, inputModel, ArchitectureModelType.PCM, inputCodeModel, additionalConfigurations, outputDir);
    }

    /**
     * Test if the results from executing ArDoCo with UML are the same as with PCM
     * 
     * @param project the project, provided by the EnumSource
     */
    @DisplayName("Compare TLR for UML/PCM")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = Project.class)
    @Order(10)
    void compareTraceLinkRecoveryForPcmAndUmlIT(Project project) {

        var ardocoRunForPCM = getArDoCoResult(project);
        Assertions.assertNotNull(ardocoRunForPCM);

        var arDoCo = ArDoCo.getInstance(name);
        var preprocessingData = ardocoRunForPCM.getPreprocessingData();
        DataRepositoryHelper.putPreprocessingData(arDoCo.getDataRepository(), preprocessingData);

        File umlModelFile = project.getModelFile(ArchitectureModelType.UML);
        File additionalConfigurations = project.getAdditionalConfigurationsFile();
        var ardocoRunForUML = arDoCo.runAndSave(name, inputText, umlModelFile, ArchitectureModelType.UML, inputCodeModel, additionalConfigurations, outputDir);
        Assertions.assertNotNull(ardocoRunForUML);

        var pcmTLs = ardocoRunForPCM.getAllTraceLinks().toList().sortThisBy(TraceLink::getModelElementUid).sortThisByInt(TraceLink::getSentenceNumber);
        var umlTLs = ardocoRunForUML.getAllTraceLinks().toList().sortThisBy(TraceLink::getModelElementUid).sortThisByInt(TraceLink::getSentenceNumber);

        Assertions.assertAll( //
                () -> Assertions.assertEquals(pcmTLs.size(), umlTLs.size()), //
                () -> Assertions.assertIterableEquals(pcmTLs, umlTLs) //
        );
    }

    /**
     * calculate {@link EvaluationResults} and compare to {@link ExpectedResults}
     * 
     * @param project      the result's project
     * @param arDoCoResult the result
     */
    private void checkResults(Project project, ArDoCoResult arDoCoResult) {

        var modelIds = arDoCoResult.getModelIds();
        var modelId = modelIds.stream().findFirst().orElseThrow();

        var goldStandard = project.getTlrGoldStandard();
        EvaluationResults<String> results = calculateResults(goldStandard.toImmutable(), arDoCoResult, modelId);

        ExpectedResults expectedResults = project.getExpectedTraceLinkResults();

        logAndSaveProjectResult(project, arDoCoResult, results, expectedResults);

        compareResultWithExpected(results, expectedResults);

    }

    private void logAndSaveProjectResult(Project project, ArDoCoResult arDoCoResult, EvaluationResults<String> results, ExpectedResults expectedResults) {
        if (logger.isInfoEnabled()) {
            String projectName = project.name().toLowerCase();
            TestUtil.logResultsWithExpected(logger, projectName, results, expectedResults);

            if (detailedDebug) {
                var data = arDoCoResult.dataRepository();
                printDetailedDebug(results, data);
                try {
                    RESULTS.add(Tuples.pair(project, TestUtil.compareTLR(DATA_MAP.get(project), TLRUtil.getTraceLinks(data), TLGoldStandardFile.loadLinks(
                            project).toImmutable())));
                    DATA_MAP.put(project, arDoCoResult);
                    PROJECT_RESULTS.add(results);
                } catch (IOException e) {
                    // failing to save project results is irrelevant for test success
                    logger.warn("Failed to load file for gold standard", e);
                }
            }
        }
    }

    private void compareResultWithExpected(EvaluationResults<String> results, ExpectedResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.precision() >= expectedResults.precision(), "Precision " + results
                        .precision() + " is below the expected minimum value " + expectedResults.precision()), //
                () -> Assertions.assertTrue(results.recall() >= expectedResults.recall(), "Recall " + results
                        .recall() + " is below the expected minimum value " + expectedResults.recall()), //
                () -> Assertions.assertTrue(results.f1() >= expectedResults.f1(), "F1 " + results
                        .f1() + " is below the expected minimum value " + expectedResults.f1()));
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.accuracy() >= expectedResults.accuracy(), "Accuracy " + results
                        .accuracy() + " is below the expected minimum value " + expectedResults.accuracy()), //
                () -> Assertions.assertTrue(results.phiCoefficient() >= expectedResults.phiCoefficient(), "Phi coefficient " + results
                        .phiCoefficient() + " is below the expected minimum value " + expectedResults.phiCoefficient()));
    }

    private static void writeDetailedOutput(Project project, ArDoCoResult arDoCoResult) {
        String name = project.name().toLowerCase();
        var path = OUTPUT_PATH.resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }
        FilePrinter.printResultsInFiles(path, name, arDoCoResult);
    }

    private EvaluationResults<String> calculateResults(ImmutableList<String> goldStandard, ArDoCoResult arDoCoResult, String modelId) {
        var traceLinks = arDoCoResult.getTraceLinksForModelAsStrings(modelId);
        logger.info("Found {} trace links", traceLinks.size());

        return TestUtil.compareTLR(arDoCoResult, traceLinks, goldStandard);
    }

    private void printDetailedDebug(EvaluationResults<String> results, DataRepository data) {
        var falseNegatives = results.falseNegatives().stream().map(Object::toString);
        var falsePositives = results.falsePositives().stream().map(Object::toString);

        var sentences = data.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText().getSentences();
        var modelStates = data.getData(ModelStates.ID, ModelStates.class).orElseThrow();

        for (String modelId : modelStates.modelIds()) {
            var instances = modelStates.getModelState(modelId).getInstances();

            var falseNegativeOutput = createOutputStrings(falseNegatives, sentences, instances);
            var falsePositivesOutput = createOutputStrings(falsePositives, sentences, instances);

            logger.debug("Model: \n{}", modelId);
            if (!falseNegativeOutput.isEmpty()) {
                logger.debug("False negatives:\n{}", String.join("\n", falseNegativeOutput));
            }
            if (!falsePositivesOutput.isEmpty()) {
                logger.debug("False positives:\n{}", String.join("\n", falsePositivesOutput));
            }
        }

    }

    private MutableList<String> createOutputStrings(Stream<String> traceLinkStrings, ImmutableList<Sentence> sentences,
            ImmutableList<ModelInstance> instances) {
        var outputList = Lists.mutable.<String>empty();
        for (var traceLinkString : traceLinkStrings.toList()) {
            var parts = traceLinkString.split(",", -1);
            if (parts.length < 2) {
                continue;
            }
            var id = parts[0];

            var modelElement = instances.detect(instance -> instance.getUid().equals(id));

            var sentence = parts[1];

            var sentenceNo = -1;
            try {
                sentenceNo = Integer.parseInt(sentence);
            } catch (NumberFormatException e) {
                logger.debug("Having problems retrieving sentence, so skipping line: {}", traceLinkString);
                continue;
            }
            var sentenceText = sentences.get(sentenceNo - 1);

            outputList.add(String.format("%-20s - %s (%s)", modelElement.getFullName(), sentenceText.getText(), traceLinkString));
        }
        return outputList;
    }

}
