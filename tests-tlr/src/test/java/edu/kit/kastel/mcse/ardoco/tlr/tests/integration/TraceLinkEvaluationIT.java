/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.tests.eval.ProjectHelper.ANALYZE_CODE_DIRECTLY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator.ResultCalculatorUtil;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.TestLink;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLDiffFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLLogFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLModelFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLPreviousFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLSentenceFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLSummaryFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TraceLinkEvaluationIT<T extends GoldStandardProject> {

    protected static final Logger logger = LoggerFactory.getLogger(TraceLinkEvaluationIT.class);

    protected static final String OUTPUT = "target/testout-tlr-it";

    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";
    protected static AtomicBoolean analyzeCodeDirectly = ANALYZE_CODE_DIRECTLY;

    protected static final List<Pair<GoldStandardProject, EvaluationResults<TestLink>>> RESULTS = new ArrayList<>();
    protected static final MutableList<EvaluationResults<String>> PROJECT_RESULTS = Lists.mutable.empty();
    protected static final Map<GoldStandardProject, ArDoCoResult> DATA_MAP = new LinkedHashMap<>();

    @BeforeAll
    static void beforeAll() {
        System.setProperty(LOGGING_ARDOCO_CORE, "info");
    }

    @AfterAll
    static void afterAll() {
        logOverallResultsForSadSamTlr();
        writeOutputForSadSamTlr();

        System.setProperty(LOGGING_ARDOCO_CORE, "error");
    }

    private static void cleanUpCodeRepository(CodeProject codeProject) {
        RepositoryHandler.removeRepository(codeProject.getCodeLocation());
    }

    private static void logOverallResultsForSadSamTlr() {
        if (logger.isInfoEnabled()) {
            var name = "Overall Weighted";
            var results = ResultCalculatorUtil.calculateWeightedAverageResults(PROJECT_RESULTS.toImmutable());
            TestUtil.logResults(logger, name, results);

            name = "Overall Macro";
            results = ResultCalculatorUtil.calculateAverageResults(PROJECT_RESULTS.toImmutable());
            TestUtil.logResults(logger, name, results);
        }
    }

    private static void writeOutputForSadSamTlr() {
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

    private static List<Project> getHistoricalProjects() {
        return filterForHistoricalProjects(List.of(Project.values()));
    }

    private static List<CodeProject> getNonHistoricalCodeProjects() {
        return filterForNonHistoricalProjects(List.of(CodeProject.values()));
    }

    private static <T extends Enum<T>> List<T> filterForHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForNonHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> !p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForProjects(Collection<T> unfilteredProjects, Predicate<T> filter) {
        List<T> projects = new ArrayList<>();
        for (var project : unfilteredProjects) {
            if (filter.test(project)) {
                projects.add(project);
            }
        }
        return projects;
    }

    @EnabledIfEnvironmentVariable(named = "testCodeFull", matches = ".*")
    @DisplayName("Evaluate SAD-SAM-Code TLR (Full)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonHistoricalCodeProjects")
    @Order(1)
    protected void evaluateSadSamCodeTlrFullIT(CodeProject project) {
        analyzeCodeDirectly.set(true);
        if (analyzeCodeDirectly.get())
            cleanUpCodeRepository(project);

        var evaluation = new SadSamCodeTraceabilityLinkRecoveryEvaluation();
        ArDoCoResult results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @EnabledIfEnvironmentVariable(named = "testCodeFull", matches = ".*")
    @DisplayName("Evaluate SAM-Code TLR (Full)")
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = CodeProject.class, mode = EnumSource.Mode.MATCH_NONE, names = "^.*HISTORICAL$")
    @Order(2)
    protected void evaluateSamCodeTlrFullIT(CodeProject project) {
        analyzeCodeDirectly.set(true);
        if (analyzeCodeDirectly.get())
            cleanUpCodeRepository(project);

        var evaluation = new SamCodeTraceabilityLinkRecoveryEvaluation();
        var results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @DisplayName("Evaluate SAD-SAM-Code TLR")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonHistoricalCodeProjects")
    @Order(9)
    protected void evaluateSadSamCodeTlrIT(CodeProject codeProject) {
        analyzeCodeDirectly.set(false);
        if (analyzeCodeDirectly.get())
            cleanUpCodeRepository(codeProject);

        var evaluation = new SadSamCodeTraceabilityLinkRecoveryEvaluation();
        var results = evaluation.runTraceLinkEvaluation(codeProject);
        Assertions.assertNotNull(results);

        TraceabilityLinkRecoveryEvaluation.resultMap.put(codeProject, results);
    }

    @DisplayName("Evaluate SAM-Code TLR")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonHistoricalCodeProjects")
    @Order(10)
    protected void evaluateSamCodeTlrIT(CodeProject project) {
        analyzeCodeDirectly.set(false);
        if (analyzeCodeDirectly.get())
            cleanUpCodeRepository(project);

        var evaluation = new SamCodeTraceabilityLinkRecoveryEvaluation();
        ArDoCoResult results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @DisplayName("Evaluate SAD-SAM TLR")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonHistoricalCodeProjects")
    @Order(20)
    protected void evaluateSadSamTlrIT(T project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluation<>();
        var results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    @EnabledIfEnvironmentVariable(named = "testHistoric", matches = ".*")
    @DisplayName("Evaluate TLR (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getHistoricalProjects")
    @Order(21)
    protected void evaluateSadSamTlrHistoricalIT(T project) {
        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluation<>();
        ArDoCoResult arDoCoResult = evaluation.getArDoCoResult(project);
        Assertions.assertNotNull(arDoCoResult);

        SadSamTraceabilityLinkRecoveryEvaluation.checkResults(project, arDoCoResult);
        SadSamTraceabilityLinkRecoveryEvaluation.writeDetailedOutput(project, arDoCoResult);
    }

    /**
     * Test if the results from executing ArDoCo with UML are the same as with PCM
     *
     * @param project the project, provided by the EnumSource
     */
    @Disabled("Only enable this for local tests.")
    @DisplayName("Compare TLR for UML/PCM")
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Project.class)
    @Order(29)
    protected void compareSadSamTlRForPcmAndUmlIT(Project project) {
        String name = project.name();
        var inputText = project.getTextFile();

        var evaluation = new SadSamTraceabilityLinkRecoveryEvaluation<>();

        var ardocoRunForPCM = evaluation.getArDoCoResult(project);
        Assertions.assertNotNull(ardocoRunForPCM);

        var arDoCo = ArDoCo.getInstance(name);
        var preprocessingData = ardocoRunForPCM.getPreprocessingData();
        DataRepositoryHelper.putPreprocessingData(arDoCo.getDataRepository(), preprocessingData);

        File umlModelFile = project.getModelFile(ArchitectureModelType.UML);
        File additionalConfigurations = project.getAdditionalConfigurationsFile();
        var ardocoRunForUML = evaluation.getArDoCoResult(name, inputText, umlModelFile, ArchitectureModelType.UML, additionalConfigurations);
        Assertions.assertNotNull(ardocoRunForUML);

        var pcmTLs = ardocoRunForPCM.getAllTraceLinks()
                .toList()
                .sortThisBy(SadSamTraceLink::getModelElementUid)
                .sortThisByInt(SadSamTraceLink::getSentenceNumber);
        var umlTLs = ardocoRunForUML.getAllTraceLinks()
                .toList()
                .sortThisBy(SadSamTraceLink::getModelElementUid)
                .sortThisByInt(SadSamTraceLink::getSentenceNumber);

        Assertions.assertAll( //
                () -> Assertions.assertEquals(pcmTLs.size(), umlTLs.size()), //
                () -> Assertions.assertIterableEquals(pcmTLs, umlTLs) //
        );
    }
}
