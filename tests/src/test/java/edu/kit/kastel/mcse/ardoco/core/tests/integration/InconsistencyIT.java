/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.baseline.DeleteOneModelElementBaselineEval;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.IEvaluationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model.DeleteOneModelElementEval;

class InconsistencyIT {
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyIT.class);

    private static final String OUTPUT = "src/test/resources/testout";

    @DisplayName("Evaluate Inconsistency Analyses")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(Project.class)
    void inconsistencyIT(Project project) {
        AbstractEvalStrategy evalStrategy = new DeleteOneModelElementEval();
        var results = evalInconsistency(project, evalStrategy, false);
        var expectedResults = project.getExpectedInconsistencyResults();

        logResults(project, results, expectedResults);
        checkResults(results, expectedResults);
    }

    @DisplayName("Evaluate Inconsistency Analyses (with Diagrams)")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(Project.class)
    void inconsistencyITDiagrams(Project project) {
        AbstractEvalStrategy evalStrategy = new DeleteOneModelElementEval();
        var results = evalInconsistency(project, evalStrategy, true);
        var expectedResults = project.getExpectedInconsistencyResults();

        logResults(project, results, expectedResults);
        checkResults(results, expectedResults);
    }

    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = "true")
    @DisplayName("Evaluate Inconsistency Analyses Baseline")
    @ParameterizedTest(name = "Evaluating Baseline For {0}")
    @EnumSource(Project.class)
    void inconsistencyBaselineIT(Project project) {
        AbstractEvalStrategy evalStrategy = new DeleteOneModelElementBaselineEval();
        var results = evalInconsistency(project, evalStrategy, false);
        var expectedResults = project.getExpectedInconsistencyResults();

        logResults(project, results, expectedResults);
        Assertions.assertTrue(results.getF1() > 0.0);
    }

    private void logResults(Project project, EvaluationResult results, EvaluationResults expectedResults) {
        if (logger.isInfoEnabled()) {
            String infoString = String.format(Locale.ENGLISH,
                    "\n%s:\n\tPrecision:\t%.3f (min. expected: %.3f)%n\tRecall:\t\t%.3f (min. expected: %.3f)%n\tF1:\t\t%.3f (min. expected: %.3f)",
                    project.name(), results.getPrecision(), expectedResults.precision, results.getRecall(), expectedResults.getRecall(), results.getF1(),
                    expectedResults.getF1());
            logger.info(infoString);
        }
    }

    private void checkResults(EvaluationResult results, EvaluationResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(),
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(),
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(),
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.getF1()));
    }

    private static EvaluationResult evalInconsistency(Project project, AbstractEvalStrategy evalStrategy, boolean withDiagrams) {
        var name = project.name();
        logger.info("Starting Inconsistency Analyses for {}", name);

        var outFile = String.format("%s%sinconsistency-eval-%s.txt", OUTPUT, File.separator, name.toLowerCase());

        try (PrintStream os = new PrintStream(outFile)) {
            return run(project, evalStrategy, os, withDiagrams);
        } catch (FileNotFoundException e) {
            Assertions.fail("Could not find file.");
        }
        return null;
    }

    private static EvaluationResult run(Project project, IEvaluationStrategy eval, PrintStream os, boolean withDiagrams) {
        os.println("####################################");
        os.println("START Eval: " + project + " -- " + eval.getClass().getSimpleName());

        IModelConnector pcmModel = project.getModel();
        IText annotatedText = project.getText();

        GoldStandard gs = project.getGoldStandard(pcmModel);
        var results = eval.evaluate(project, pcmModel, annotatedText, gs, os, withDiagrams);

        os.println("END Eval: " + project + " -- " + eval);
        os.println("####################################\n");
        return results;
    }
}
