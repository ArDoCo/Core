/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExplicitEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.OverallResultsCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLDiffFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLLogFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLModelFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLPreviousFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLSentenceFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLSummaryFile;

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
    private static final List<TLProjectEvalResult> RESULTS = new ArrayList<>();
    private static final Map<Project, ArDoCoResult> DATA_MAP = new EnumMap<>(Project.class);
    private static final boolean detailedDebug = true;
    private static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    private String name;
    private File inputText;
    private File inputModel;
    private File inputCodeModel = null;
    private File additionalConfigs = null;
    private final File outputDir = new File(OUTPUT);

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    public static void afterAll() {
        if (logger.isInfoEnabled()) {
            OverallResultsCalculator overallResultsCalculator = TestUtil.getOverallResultsCalculator(RESULTS);
            var name = "Overall Weighted";
            var results = overallResultsCalculator.calculateWeightedAveragePRF1();
            TestUtil.logResults(logger, name, results);

            name = "Overall Macro";
            results = overallResultsCalculator.calculateMacroAveragePRF1();
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
                TLPreviousFile.save(evalDir.resolve("previous.csv"), RESULTS); // save before loading
                TLDiffFile.save(evalDir.resolve("diff.txt"), RESULTS, TLPreviousFile.load(evalDir.resolve("previous.csv")), DATA_MAP);
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
        if (additionalConfigs != null) {
            additionalConfigs = null;
        }
    }

    // NOTE: if you only want to test a specific project, you can simply set up the
    // EnumSource. For more details, see
    // https://www.baeldung.com/parameterized-tests-junit-5#3-enum
    // Example: add ", names = { "BIGBLUEBUTTON" }" to EnumSource
    // However, make sure to revert this before you commit and push!
    @DisplayName("Evaluate TLR")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = Project.class)
    @Order(1)
    void evaluateTraceLinkRecoveryIT(Project project) {
        ArDoCoResult arDoCoResult = getArDoCoResult(project);
        Assertions.assertNotNull(arDoCoResult);

        // calculate results and compare to expected results
        checkResults(project, arDoCoResult);

        writeDetailedOutput(project, arDoCoResult);
    }

    private ArDoCoResult getArDoCoResult(Project project) {
        inputModel = project.getModelFile();
        inputText = project.getTextFile();
        name = project.name().toLowerCase();
        ArDoCo arDoCo = ArDoCo.getInstance(name);

        var arDoCoResult = DATA_MAP.get(project);
        if (arDoCoResult == null) {
            arDoCoResult = arDoCo.runAndSave(name, inputText, inputModel, ArchitectureModelType.PCM, inputCodeModel, additionalConfigs, outputDir);
            DATA_MAP.put(project, arDoCoResult);
        }
        return arDoCoResult;
    }

    /**
     * Test if the results from executing ArDoCo with UML are the same as with PCM
     * 
     * @param project the project, provided by the EnumSource
     */
    @DisplayName("Compare TLR for UML/PCM")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = Project.class)
    @Order(2)
    void compareTraceLinkRecoveryForPcmAndUmlIT(Project project) {
        var ardocoRunForPCM = getArDoCoResult(project);
        Assertions.assertNotNull(ardocoRunForPCM);

        var arDoCo = ArDoCo.getInstance(name);
        var preprocessingData = ardocoRunForPCM.getPreprocessingData();
        DataRepositoryHelper.putPreprocessingData(arDoCo.getDataRepository(), preprocessingData);
        File umlModelFile = project.getModelFile(ArchitectureModelType.UML);
        var ardocoRunForUML = arDoCo.runAndSave(name, inputText, umlModelFile, ArchitectureModelType.UML, inputCodeModel, additionalConfigs, outputDir);
        Assertions.assertNotNull(ardocoRunForUML);

        var pcmTLs = ardocoRunForPCM.getAllTraceLinks().toList().sortThisBy(TraceLink::getModelElementUid).sortThisByInt(TraceLink::getSentenceNumber);
        var umlTLs = ardocoRunForUML.getAllTraceLinks().toList().sortThisBy(TraceLink::getModelElementUid).sortThisByInt(TraceLink::getSentenceNumber);

        Assertions.assertAll( //
                () -> Assertions.assertEquals(pcmTLs.size(), umlTLs.size()), //
                () -> Assertions.assertIterableEquals(pcmTLs, umlTLs) //
        );
    }

    private void checkResults(Project project, ArDoCoResult arDoCoResult) {
        String name = project.name().toLowerCase();
        var modelIds = arDoCoResult.getModelIds();
        var modelId = modelIds.stream().findFirst().orElseThrow();
        var model = arDoCoResult.getModelState(modelId);

        var results = calculateResults(project, arDoCoResult, model);
        var expectedResults = project.getExpectedTraceLinkResults();
        var data = arDoCoResult.dataRepository();

        if (logger.isInfoEnabled()) {
            TestUtil.logResultsWithExpected(logger, name, results, expectedResults);

            if (detailedDebug) {
                if (results instanceof ExplicitEvaluationResults<?> explicitResults) {
                    printDetailedDebug(explicitResults, data);
                }
                try {
                    RESULTS.add(new TLProjectEvalResult(project, data));
                    DATA_MAP.put(project, arDoCoResult);
                } catch (IOException e) {
                    // failing to save project results is irrelevant for test success
                    logger.warn("Failed to load file for gold standard", e);
                }
            }
        }

        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(), "Precision " + results
                        .getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(), "Recall " + results
                        .getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(), "F1 " + results
                        .getF1() + " is below the expected minimum value " + expectedResults.getF1()));
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

    private EvaluationResults calculateResults(Project project, ArDoCoResult arDoCoResult, ModelExtractionState modelState) {
        String modelId = modelState.getModelId();
        var traceLinks = arDoCoResult.getTraceLinksForModelAsStrings(modelId);
        logger.info("Found {} trace links", traceLinks.size());

        var goldStandard = getGoldStandard(project);

        return TestUtil.compare(traceLinks.toSet(), goldStandard);
    }

    private List<String> getGoldStandard(Project project) {
        var path = Paths.get(project.getGoldStandardFile().toURI());
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove(0);
        return goldLinks;
    }

    private void printDetailedDebug(ExplicitEvaluationResults<?> results, DataRepository data) {
        var falseNegatives = results.getFalseNegatives().stream().map(Object::toString);
        var falsePositives = results.getFalsePositives().stream().map(Object::toString);

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
