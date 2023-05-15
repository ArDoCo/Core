/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.CodeUtils;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ResultMatrix;

public abstract class TraceabilityLinkRecoveryEvaluation {
    protected static final Logger logger = LoggerFactory.getLogger(SamCodeTraceabilityLinkRecoveryEvaluationIT.class);

    protected static final String OUTPUT = "src/test/resources/testout";
    protected final File outputDir = new File(OUTPUT);
    protected static final String ADDITIONAL_CONFIG = null;

    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";
    protected static boolean analyzeCodeDirectly = false;
    protected static Map<Project, ArDoCoResult> resultMap = new EnumMap<>(Project.class);

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "error");

        // Remove repositories
        if (analyzeCodeDirectly) {
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
        analyzeCodeDirectly = false;
        var results = runTraceLinkEvaluation(project);
        resultMap.put(project.getProject(), results);
    }

    @EnabledIfEnvironmentVariable(named = "testCodeFull", matches = ".*")
    @DisplayName("Evaluate TLR (Full)")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(value = CodeProject.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(1)
    void evaluateTraceLinkRecoveryFullIT(CodeProject project) {
        analyzeCodeDirectly = true;
        runTraceLinkEvaluation(project);
    }

    protected ArDoCoResult runTraceLinkEvaluation(CodeProject codeProject) {
        var project = codeProject.getProject();
        ArDoCoResult result = resultMap.get(project);
        if (result != null && resultHasRequiredData(result)) {
            return result;
        } else {
            ArDoCoRunner runner = getAndSetupRunner(codeProject);
            result = runner.run();
        }
        Assertions.assertNotNull(result);

        var goldStandard = getGoldStandard(codeProject);
        var evaluationResults = calculateEvaluationResults(result, goldStandard);

        ExpectedResults expectedResults = getExpectedResults(codeProject);
        TestUtil.logExtendedResultsWithExpected(logger, codeProject.name(), evaluationResults, expectedResults);
        compareResults(evaluationResults, expectedResults);
        return result;
    }

    protected abstract boolean resultHasRequiredData(ArDoCoResult potentialResults);

    protected File getInputCode(CodeProject codeProject) {
        File inputCode;
        if (analyzeCodeDirectly) {
            prepareCode(codeProject);
            inputCode = new File(codeProject.getCodeLocation());
        } else {
            inputCode = new File(codeProject.getCodeModelLocation());
        }
        return inputCode;
    }

    protected abstract ArDoCoRunner getAndSetupRunner(CodeProject codeProject);

    protected static Map<String, String> getAdditionalConfigsMap() {
        Map<String, String> additionalConfigsMap;
        if (ADDITIONAL_CONFIG != null) {
            additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(ADDITIONAL_CONFIG));
        } else {
            additionalConfigsMap = new HashMap<>();
        }
        return additionalConfigsMap;
    }

    private void prepareCode(CodeProject codeProject) {
        File codeLocation = new File(codeProject.getCodeLocation());

        if (!codeLocation.exists()) {
            CodeUtils.shallowCloneRepository(codeProject.getCodeRepository(), codeProject.getCodeLocation());
        }
    }

    protected abstract ExpectedResults getExpectedResults(CodeProject codeProject);

    protected abstract ImmutableList<String> getGoldStandard(CodeProject codeProject);

    protected void compareResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
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

    /**
     * compares the tlr results with the expected results and creates a new {@link EvaluationResults}.
     *
     * @param arDoCoResult the {@link ArDoCoResult ArDoCoResults}
     * @param goldStandard Collection representing the gold standard
     * @return the result of the comparison
     */
    protected EvaluationResults<String> calculateEvaluationResults(ArDoCoResult arDoCoResult, ImmutableCollection<String> goldStandard) {
        ImmutableList<String> results = createTraceLinkStringList(arDoCoResult);
        Assertions.assertFalse(results.isEmpty());

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

        int trueNegatives = getConfusionMatrixSum(arDoCoResult) - truePositives.size() - falsePositives.size() - falseNegatives.size();
        return EvaluationResults.createEvaluationResults(new ResultMatrix<>(truePositivesList, trueNegatives, falsePositivesList, falseNegativesList));
    }

    protected abstract ImmutableList<String> createTraceLinkStringList(ArDoCoResult arDoCoResult);

    protected abstract int getConfusionMatrixSum(ArDoCoResult arDoCoResult);

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
