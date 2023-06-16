/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.tests.integration.TraceLinkEvaluationIT.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCoForSadSamTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLRUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLGoldStandardFile;

/**
 * Integration test that evaluates the traceability link recovery capabilities of ArDoCo. Runs on the projects that are defined in the enum {@link Project}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SadSamTraceabilityLinkRecoveryEvaluation extends TraceabilityLinkRecoveryEvaluation {

    @Override
    protected boolean resultHasRequiredData(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getAllTraceLinks();
        return !traceLinks.isEmpty();
    }

    @Override
    protected ArDoCoResult runTraceLinkEvaluation(CodeProject codeProject) {
        var result = super.runTraceLinkEvaluation(codeProject);
        DATA_MAP.put(codeProject.getProject(), result);
        return result;
    }

    @Override
    protected ArDoCoRunner getAndSetupRunner(CodeProject codeProject) {
        var project = codeProject.getProject();
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(project.getAdditionalConfigurationsFile());

        String name = project.name().toLowerCase();
        File inputModel = project.getModelFile();
        File inputText = project.getTextFile();
        File outputDir = new File(OUTPUT);

        var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(name);
        runner.setUp(inputText, inputModel, ArchitectureModelType.PCM, additionalConfigsMap, outputDir);
        return runner;
    }

    @Override
    protected ExpectedResults getExpectedResults(CodeProject codeProject) {
        var project = codeProject.getProject();
        return project.getExpectedTraceLinkResults();
    }

    @Override
    protected ImmutableList<String> getGoldStandard(CodeProject codeProject) {
        var project = codeProject.getProject();
        return project.getTlrGoldStandard();
    }

    @Override
    protected ImmutableList<String> enrollGoldStandard(ImmutableList<String> goldStandard, ArDoCoResult result) {
        return goldStandard;
    }

    @Override
    protected ImmutableList<String> createTraceLinkStringList(ArDoCoResult arDoCoResult) {
        var sadSamTls = Lists.immutable.ofAll(arDoCoResult.getAllTraceLinks());
        return TraceLinkUtilities.getSadSamTraceLinksAsStringList(sadSamTls);
    }

    @Override
    protected int getConfusionMatrixSum(ArDoCoResult arDoCoResult) {
        int sentences = arDoCoResult.getText().getSentences().size();
        int modelElements = 0;
        for (var model : arDoCoResult.getModelIds()) {
            modelElements += arDoCoResult.getModelState(model).getInstances().size();
        }

        return sentences * modelElements;
    }

    @Override
    protected EvaluationResults<String> calculateEvaluationResults(ArDoCoResult arDoCoResult, ImmutableCollection<String> goldStandard) {
        var results = super.calculateEvaluationResults(arDoCoResult, goldStandard);
        PROJECT_RESULTS.add(results);
        return results;
    }

    protected ArDoCoResult getArDoCoResult(Project project) {
        String name = project.name().toLowerCase();
        var inputModel = project.getModelFile();
        var inputText = project.getTextFile();

        var arDoCoResult = DATA_MAP.get(project);
        if (arDoCoResult == null) {
            File additionalConfigurations = project.getAdditionalConfigurationsFile();
            arDoCoResult = getArDoCoResult(name, inputText, inputModel, ArchitectureModelType.PCM, additionalConfigurations);
            DATA_MAP.put(project, arDoCoResult);
        }
        return arDoCoResult;
    }

    protected ArDoCoResult getArDoCoResult(String name, File inputText, File inputModel, ArchitectureModelType architectureModelType,
            File additionalConfigurations) {
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(additionalConfigurations);
        File outputDir = new File(OUTPUT);

        var runner = new ArDoCoForSadSamTraceabilityLinkRecovery(name);
        runner.setUp(inputText, inputModel, architectureModelType, additionalConfigsMap, outputDir);
        return runner.run();
    }

    /**
     * calculate {@link EvaluationResults} and compare to {@link ExpectedResults}
     *
     * @param project      the result's project
     * @param arDoCoResult the result
     */
    protected static void checkResults(Project project, ArDoCoResult arDoCoResult) {

        var modelIds = arDoCoResult.getModelIds();
        var modelId = modelIds.stream().findFirst().orElseThrow();

        var goldStandard = project.getTlrGoldStandard();
        EvaluationResults<String> results = calculateResults(goldStandard, arDoCoResult, modelId);

        ExpectedResults expectedResults = project.getExpectedTraceLinkResults();

        logAndSaveProjectResult(project, arDoCoResult, results, expectedResults);

        compareResultWithExpected(results, expectedResults);

    }

    private static void logAndSaveProjectResult(Project project, ArDoCoResult arDoCoResult, EvaluationResults<String> results,
            ExpectedResults expectedResults) {
        if (logger.isInfoEnabled()) {
            String projectName = project.name().toLowerCase();
            TestUtil.logResultsWithExpected(logger, projectName, results, expectedResults);

            var data = arDoCoResult.dataRepository();
            printDetailedDebug(results, data);
            try {
                RESULTS.add(Tuples.pair(project, TestUtil.compareTLR(DATA_MAP.get(project), TLRUtil.getTraceLinks(data), TLGoldStandardFile.loadLinks(project)
                        .toImmutable())));
                DATA_MAP.put(project, arDoCoResult);
                PROJECT_RESULTS.add(results);
            } catch (IOException e) {
                // failing to save project results is irrelevant for test success
                logger.warn("Failed to load file for gold standard", e);
            }
        }
    }

    private static void compareResultWithExpected(EvaluationResults<String> results, ExpectedResults expectedResults) {
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

    static void writeDetailedOutput(Project project, ArDoCoResult arDoCoResult) {
        String name = project.name().toLowerCase();
        var path = Path.of(OUTPUT).resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.warn("Could not create directories.", e);
        }
        FilePrinter.printResultsInFiles(path, name, arDoCoResult);
    }

    private static EvaluationResults<String> calculateResults(ImmutableList<String> goldStandard, ArDoCoResult arDoCoResult, String modelId) {
        var traceLinks = arDoCoResult.getTraceLinksForModelAsStrings(modelId);
        logger.info("Found {} trace links", traceLinks.size());

        return TestUtil.compareTLR(arDoCoResult, traceLinks, goldStandard);
    }

    private static void printDetailedDebug(EvaluationResults<String> results, DataRepository data) {
        var falseNegatives = results.falseNegatives().stream().map(Object::toString);
        var falsePositives = results.falsePositives().stream().map(Object::toString);

        var sentences = data.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText().getSentences();
        var modelStates = data.getData(ModelStates.ID, ModelStates.class).orElseThrow();

        for (String modelId : modelStates.extractionModelIds()) {
            var instances = modelStates.getModelExtractionState(modelId).getInstances();

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

    private static MutableList<String> createOutputStrings(Stream<String> traceLinkStrings, ImmutableList<Sentence> sentences,
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
