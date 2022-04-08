/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.IEvaluationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model.DeleteOneModelElementEval;

class InconsistencyIT {
    private static Logger logger = null;

    private static final String OUTPUT = "src/test/resources/testout";

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("log4j.configurationFile", "src/main/resources/log4j2.xml");
        logger = LogManager.getLogger(InconsistencyIT.class);
    }

    @DisplayName("Evaluate Inconsistency Analyses")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(Project.class)
    void inconsistencyIT(Project project) {
        var results = evalInconsistency(project);
        var expectedResults = project.getExpectedInconsistencyResults();

        if (logger.isInfoEnabled()) {
            String infoString = String.format(Locale.ENGLISH,
                    "\n%s:\n\tPrecision:\t%.3f (min. expected: %.3f)%n\tRecall:\t\t%.3f (min. expected: %.3f)%n\tF1:\t\t%.3f (min. expected: %.3f)",
                    project.name(), results.getPrecision(), expectedResults.precision, results.getRecall(), expectedResults.getRecall(), results.getF1(),
                    expectedResults.getF1());
            logger.info(infoString);
        }

        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(),
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(),
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(),
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.getF1()));
    }

    private static EvaluationResult evalInconsistency(Project project) {
        var name = project.name();
        logger.info("Starting Inconsistency Analyses for {}", name);
        AbstractEvalStrategy eval1 = new DeleteOneModelElementEval();

        var outFile = String.format("%s%sinconsistency-eval-%s.txt", OUTPUT, File.separator, name.toLowerCase());

        try (PrintStream os = new PrintStream(new File(outFile))) {
            var results = run(project, eval1, os);
            return results;
        } catch (FileNotFoundException e) {
            Assertions.assertTrue(false, "Could not find file.");
        }
        return null;
    }

    private static EvaluationResult run(Project project, IEvaluationStrategy eval, PrintStream os) {
        os.println("####################################");
        os.println("START Eval: " + project + " -- " + eval.getClass().getSimpleName());

        IModelConnector pcmModel = project.getModel();
        IText annotatedText = project.getText();

        GoldStandard gs = project.getGoldStandard(pcmModel);
        var results = eval.evaluate(project, pcmModel, annotatedText, gs, os);

        os.println("END Eval: " + project + " -- " + eval);
        os.println("####################################\n");
        return results;
    }
}
