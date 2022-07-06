/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLDiffFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLLogFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLModelFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLPreviousFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLSentenceFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLSummaryFile;

/**
 * @author Jan Keim
 *
 */
class TracelinksIT {
    private static final Logger logger = LoggerFactory.getLogger(TracelinksIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String ADDITIONAL_CONFIG = null;
    private static final List<TLProjectEvalResult> RESULTS = new ArrayList<>();
    private static final Map<Project, DataStructure> DATA_MAP = new HashMap<>();
    private static final boolean detailedDebug = true;

    private File inputText;
    private File inputModel;
    private File additionalConfigs = null;
    private final File outputDir = new File(OUTPUT);

    @AfterAll
    public static void afterAll() throws IOException {
        if (detailedDebug) {
            var evalDir = Path.of(OUTPUT).resolve("tl_eval");
            Files.createDirectories(evalDir);

            TLSummaryFile.save(evalDir.resolve("summary.md"), RESULTS, DATA_MAP);
            TLModelFile.save(evalDir.resolve("models.md"), DATA_MAP);
            TLSentenceFile.save(evalDir.resolve("sentences.md"), DATA_MAP);
            TLLogFile.append(evalDir.resolve("log.md"), RESULTS);
            TLPreviousFile.save(evalDir.resolve("previous.csv"), RESULTS); // save before loading
            TLDiffFile.save(evalDir.resolve("diff.md"), RESULTS, TLPreviousFile.load(evalDir.resolve("previous.csv")), DATA_MAP);
        }
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
    // EnumSource
    // For more details, see
    // https://www.baeldung.com/parameterized-tests-junit-5#3-enum
    // Example: add ", names = { "BIGBLUEBUTTON" }" to EnumSource
    // However, make sure to revert this before you commit and push!
    @DisplayName("Evaluate TLR (Text-based)")
    @ParameterizedTest(name = "Evaluating {0} (Text)")
    @EnumSource(value = Project.class)
    void compareTraceLinksTextIT(Project project) {
        compare(project);
    }

    private void compare(Project project) {
        var name = project.name().toLowerCase();
        inputModel = project.getModelFile();
        inputText = project.getTextFile();

        // execute pipeline
        DataStructure dataStructure = ArDoCo.runAndSave("test_" + name, inputText, inputModel, null, additionalConfigs, outputDir);

        var data = dataStructure.dataRepository();
        Assertions.assertNotNull(data);
        var modelIds = dataStructure.getModelIds();
        Assertions.assertEquals(1, modelIds.size());
        var modelId = modelIds.stream().findFirst().orElseThrow();
        var model = dataStructure.getModelState(modelId);

        // calculate results and compare to expected results
        var results = calculateResults(project, dataStructure, model);
        var expectedResults = project.getExpectedTraceLinkResults();

        if (logger.isInfoEnabled()) {
            var infoString = String.format(Locale.ENGLISH,
                    "\n%s:\n\tPrecision:\t%.3f (min. expected: %.3f)%n\tRecall:\t\t%.3f (min. expected: %.3f)%n\tF1:\t\t%.3f (min. expected: %.3f)", name,
                    results.getPrecision(), expectedResults.getPrecision(), results.getRecall(), expectedResults.getRecall(), results.getF1(),
                    expectedResults.getF1());
            logger.info(infoString);

            if (detailedDebug) {
                printDetailedDebug(results, data);
                try {
                    RESULTS.add(new TLProjectEvalResult(project, data));
                    DATA_MAP.put(project, dataStructure);
                } catch (IOException e) {
                    // failing to save project results is irrelevant for test success
                    logger.warn(e.getMessage(), e.getCause());
                }
            }

        }

        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(),
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(),
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(),
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.getF1()));

    }

    private void printDetailedDebug(EvaluationResults results, DataRepository data) {
        var falseNegatives = results.getFalseNegative().stream().map(Object::toString);
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

    private MutableList<String> createOutputStrings(Stream<String> tracelinkStrings, ImmutableList<Sentence> sentences,
            ImmutableList<ModelInstance> instances) {
        var outputList = Lists.mutable.<String> empty();
        for (var tracelinkString : tracelinkStrings.toList()) {
            var parts = tracelinkString.split(",");
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
                logger.debug("Having problems retrieving sentence, so skipping line: {}", tracelinkString);
                continue;
            }
            var sentenceText = sentences.get(sentenceNo - 1);

            outputList.add(String.format("%-20s - %s (%s)", modelElement.getFullName(), sentenceText.getText(), tracelinkString));
        }
        return outputList;
    }

    private EvaluationResults calculateResults(Project project, DataStructure dataStructure, ModelExtractionState modelState) {
        var connectionState = dataStructure.getConnectionState(modelState.getModelId());
        var traceLinks = getTraceLinksFromConnectionState(connectionState);
        logger.info("Found {} trace links", traceLinks.size());

        var goldStandard = getGoldStandard(project);

        return TestUtil.compare(traceLinks, goldStandard);
    }

    private Set<String> getTraceLinksFromConnectionState(ConnectionState connectionState) {
        var formatString = "%s,%d";
        return connectionState.getTraceLinks().collect(tl -> String.format(formatString, tl.getModelElementUid(), tl.getSentenceNumber() + 1)).castToSet();
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
}
