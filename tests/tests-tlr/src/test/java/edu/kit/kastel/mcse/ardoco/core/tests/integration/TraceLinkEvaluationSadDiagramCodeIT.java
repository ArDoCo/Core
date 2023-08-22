/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.tests.eval.ProjectHelper.ANALYZE_CODE_DIRECTLY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator.ResultCalculatorUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TestLink;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.*;

class TraceLinkEvaluationSadDiagramCodeIT {
    protected static final Logger logger = LoggerFactory.getLogger(TraceLinkEvaluationIT.class);

    protected static final String OUTPUT = "src/test/resources/testout";

    protected static final String LOGGING_ARDOCO_CORE = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.core";

    protected static final List<Pair<Project, EvaluationResults<TestLink>>> RESULTS = new ArrayList<>();
    protected static final MutableList<EvaluationResults<String>> PROJECT_RESULTS = Lists.mutable.empty();
    protected static final Map<Project, ArDoCoResult> DATA_MAP = new EnumMap<>(Project.class);

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

    @DisplayName("Evaluate SAD-Diagram-Code TLR")
    @ParameterizedTest(name = "{0}")
    @EnumSource(DiagramProject.class)
    void evaluateSadDiagramCodeTlrIT(DiagramProject project) {
        ANALYZE_CODE_DIRECTLY.set(false);
        var evaluation = new SadDiagramCodeTraceabilityLinkRecoveryEvaluation();
        ArDoCoResult results = evaluation.runTraceLinkEvaluation(project.getCodeProject());
        Assertions.assertNotNull(results);
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
        var evalDir = Path.of(OUTPUT).resolve("ardoco_diagram_eval_tl");
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
