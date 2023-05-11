/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.CodeUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ResultMatrix;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArCoTLEvaluationIT {
    private static final Logger logger = LoggerFactory.getLogger(ArCoTLEvaluationIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private final File outputDir = new File(OUTPUT);
    private static final String ADDITIONAL_CONFIG = null;

    private static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";
    private static final boolean removeRepositories = true;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    public static void afterAll() {
        // TODO write results
        System.setProperty(LOGGING_ARDOCO_CORE, "error");

        // Remove repositories
        if (removeRepositories) {
            for (CodeProject codeProject : CodeProject.values()) {
                CodeUtils.removeCodeFolder(codeProject.getCodeLocation());
            }
        }
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
    @EnumSource(value = CodeProject.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(1)
    void evaluateTraceLinkRecoveryIT(CodeProject project) {
        runTraceLinkEvaluation(project);
    }

    private void runTraceLinkEvaluation(CodeProject project) {
        String name = project.name().toLowerCase();

        prepareCode(project);

        File inputArchitectureModel = project.getProject().getModelFile();
        File inputCode = new File(project.getCodeLocation());
        Map<String, String> additionalConfigsMap;
        if (ADDITIONAL_CONFIG != null) {
            additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIG));
        } else {
            additionalConfigsMap = new HashMap<>();
        }

        var runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(name);
        runner.setUp(inputArchitectureModel, ArchitectureModelType.PCM, inputCode, additionalConfigsMap, outputDir);

        var result = runner.run();
        Assertions.assertNotNull(result);

        var goldstandard = project.getSamCodeGoldStandard();
        var evaluationResults = calculateEvaluationResults(result, goldstandard);

        // TODO
        ExpectedResults expectedResults = project.getExpectedResults();
        TestUtil.logExtendedResultsWithExpected(logger, name, evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
    }

    private void compareResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.precision() >= expectedResults.precision(),
                        "Precision " + results.precision() + " is below the expected minimum value " + expectedResults.precision()), //
                () -> Assertions.assertTrue(results.recall() >= expectedResults.recall(),
                        "Recall " + results.recall() + " is below the expected minimum value " + expectedResults.recall()), //
                () -> Assertions.assertTrue(results.f1() >= expectedResults.f1(),
                        "F1 " + results.f1() + " is below the expected minimum value " + expectedResults.f1()));
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.accuracy() >= expectedResults.accuracy(),
                        "Accuracy " + results.accuracy() + " is below the expected minimum value " + expectedResults.accuracy()), //
                () -> Assertions.assertTrue(results.phiCoefficient() >= expectedResults.phiCoefficient(),
                        "Phi coefficient " + results.phiCoefficient() + " is below the expected minimum value " + expectedResults.phiCoefficient()));
    }

    private boolean prepareCode(CodeProject codeProject) {
        File codeLocation = new File(codeProject.getCodeLocation());

        if (!codeLocation.exists()) {
            return CodeUtils.shallowCloneRepository(codeProject.getCodeRepository(), codeProject.getCodeLocation());
        }
        return true;
    }

    /**
     * compares the tlr results with the expected results and creates a new {@link EvaluationResults}.
     *
     * @param arDoCoResult the {@link ArDoCoResult ArDoCoResults}
     * @param goldStandard Collection representing the gold standard
     * @return the result of the comparison
     */
    private static EvaluationResults<String> calculateEvaluationResults(ArDoCoResult arDoCoResult, ImmutableCollection<String> goldStandard) {
        var traceLinks = arDoCoResult.getSamCodeTraceLinks();
        Assertions.assertFalse(traceLinks.isEmpty());
        logger.info("Project {} with {} trace links.", arDoCoResult.getProjectName(), traceLinks.size());
        MutableList<String> resultsMut = Lists.mutable.empty();

        for (var traceLink : traceLinks) {
            EndpointTuple endpointTuple = traceLink.getEndpointTuple();
            var modelElement = endpointTuple.firstEndpoint();
            var codeElement = (CodeCompilationUnit) endpointTuple.secondEndpoint();
            String codeElementString = codeElement.toString() + "#" + codeElement.getName();
            String traceLinkString = TestUtil.createTraceLinkString(modelElement.getId(), codeElementString);
            resultsMut.add(traceLinkString);
        }
        ImmutableList<String> results = resultsMut.toImmutable();

        Set<String> distinctTraceLinks = new HashSet<>(results.castToCollection());
        Set<String> distinctGoldStandard = new HashSet<>(goldStandard.castToCollection());

        // True Positives are the trace links that are contained on both lists
        Set<String> truePositives = distinctTraceLinks.stream()
                                                      .filter(tl -> isTraceLinkContainedInGoldStandard(tl, distinctGoldStandard))
                                                      .collect(Collectors.toSet());
        ImmutableList<String> truePositivesList = Lists.immutable.ofAll(truePositives);

        // False Positives are the trace links that are only contained in the result set
        Set<String> falsePositives = distinctTraceLinks.stream()
                                                       .filter(tl -> !isTraceLinkContainedInGoldStandard(tl, distinctGoldStandard))
                                                       .collect(Collectors.toSet());
        ImmutableList<String> falsePositivesList = Lists.immutable.ofAll(falsePositives);

        // False Negatives are the trace links that are only contained in the gold standard
        Set<String> falseNegatives = distinctGoldStandard.stream()
                                                         .filter(gstl -> !isGoldStandardTraceLinkContainedInTraceLinks(gstl, distinctTraceLinks))
                                                         .collect(Collectors.toSet());
        ImmutableList<String> falseNegativesList = Lists.immutable.ofAll(falseNegatives);

        int trueNegatives = getTrueNegatives(arDoCoResult);
        return EvaluationResults.createEvaluationResults(new ResultMatrix<>(truePositivesList, trueNegatives, falsePositivesList, falseNegativesList));
    }

    private static int getTrueNegatives(ArDoCoResult arDoCoResult) {
        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(arDoCoResult.dataRepository());
        Model codeModel = modelStatesData.getModel(CodeModelType.CODE_MODEL.getModelId());
        Model architectureModel = modelStatesData.getModel(ArchitectureModelType.PCM.getModelId());
        var codeModelEndpoints = codeModel.getEndpoints().size();
        var architectureModelEndpoints = architectureModel.getEndpoints().size();
        return codeModelEndpoints * architectureModelEndpoints;
    }

    private static boolean areTraceLinksMatching(String goldStandardTraceLink, String traceLink) {
        return goldStandardTraceLink.equals(traceLink) || traceLink.startsWith(goldStandardTraceLink);
    }

    private static boolean isTraceLinkContainedInGoldStandard(String traceLink, Set<String> goldStandard) {
        return goldStandard.stream().anyMatch(goldStandardTraceLink -> areTraceLinksMatching(goldStandardTraceLink, traceLink));
    }

    private static boolean isGoldStandardTraceLinkContainedInTraceLinks(String goldStandardTraceLink, Set<String> traceLinks) {
        return traceLinks.stream().anyMatch(traceLink -> areTraceLinksMatching(goldStandardTraceLink, traceLink));
    }
}
