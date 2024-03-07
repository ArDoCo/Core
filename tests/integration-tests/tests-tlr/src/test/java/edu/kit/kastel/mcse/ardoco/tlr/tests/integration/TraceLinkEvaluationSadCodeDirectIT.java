/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.tests.eval.ProjectHelper.ANALYZE_CODE_DIRECTLY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator.ResultCalculatorUtil;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.TestLink;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLDiffFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLLogFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLModelFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLPreviousFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLSentenceFile;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files.TLSummaryFile;

class TraceLinkEvaluationSadCodeDirectIT {
    protected static final Logger logger = LoggerFactory.getLogger(TraceLinkEvaluationIT.class);

    protected static final String OUTPUT = "target/testout";

    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

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

    @DisplayName("Evaluate SAD-Code TLR")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonHistoricalCodeProjects")
    void evaluateSadCodeTlrIT(CodeProject project) {
        ANALYZE_CODE_DIRECTLY.set(false);
        var evaluation = new SadCodeTraceabilityLinkRecoveryEvaluation();
        ArDoCoResult results = evaluation.runTraceLinkEvaluation(project);
        Assertions.assertNotNull(results);
    }

    private static List<CodeProject> getNonHistoricalCodeProjects() {
        return filterForNonHistoricalProjects(List.of(CodeProject.values()));
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
}
