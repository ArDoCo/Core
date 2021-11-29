package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.IEvaluationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model.DeleteOneModelElementEval;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;

class InconsistencyIT {
    private static final Logger logger = LogManager.getLogger(InconsistencyIT.class);

    private static final String OUTPUT = "src/test/resources/testout";

    private static final double MIN_PREC_TEAMMATES = .853;
    private static final double MIN_REC_TEAMMATES = .853;
    private static final double MIN_F1_TEAMMATES = .853;

    private static final double MIN_PREC_MEDIASTORE = .90;
    private static final double MIN_REC_MEDIASTORE = .24;
    private static final double MIN_F1_MEDIASTORE = .387;

    private static final double MIN_PREC_TEASTORE = .423;
    private static final double MIN_REC_TEASTORE = .785;
    private static final double MIN_F1_TEASTORE = .550;

    @BeforeEach
    void beforeEach() {
        // set the cache to true (default setting)
        // if another tests does not want to have a cache they can manipulate themselves
        OntologyTextProvider.enableCache(true);
    }

    @Test
    @DisplayName("Evaluate Inconsistency Analyses for Teammates")
    void inconsistencyTeammatesIT() {
        var results = evalInconsistency(Project.TEAMMATES);
        Assertions.assertNotNull(results);

        var precision = results.getPrecision();
        var recall = results.getRecall();
        var f1 = results.getF1();

        Assertions.assertAll(//
                () -> Assertions.assertTrue(precision >= MIN_PREC_TEAMMATES,
                        "Precision " + precision + " is below the expected minimum value " + MIN_PREC_TEAMMATES), //
                () -> Assertions.assertTrue(recall >= MIN_REC_TEAMMATES, "Recall " + recall + " is below the expected minimum value " + MIN_REC_TEAMMATES), //
                () -> Assertions.assertTrue(f1 >= MIN_F1_TEAMMATES, "F1 " + f1 + " is below the expected minimum value " + MIN_F1_TEAMMATES));

    }

    @Test
    @DisplayName("Evaluate Inconsistency Analyses for Mediastore")
    void inconsistencyMediastoreIT() {
        var results = evalInconsistency(Project.MEDIASTORE);
        Assertions.assertNotNull(results);

        var precision = results.getPrecision();
        var recall = results.getRecall();
        var f1 = results.getF1();

        Assertions.assertAll(//
                () -> Assertions.assertTrue(precision >= MIN_PREC_MEDIASTORE,
                        "Precision " + precision + " is below the expected minimum value " + MIN_PREC_MEDIASTORE), //
                () -> Assertions.assertTrue(recall >= MIN_REC_MEDIASTORE, "Recall " + recall + " is below the expected minimum value " + MIN_REC_MEDIASTORE), //
                () -> Assertions.assertTrue(f1 >= MIN_F1_MEDIASTORE, "F1 " + f1 + " is below the expected minimum value " + MIN_F1_MEDIASTORE));
    }

    @Test
    @DisplayName("Evaluate Inconsistency Analyses for Teastore")
    void inconsistencyTeastoreIT() {
        var results = evalInconsistency(Project.TEASTORE);
        Assertions.assertNotNull(results);

        var precision = results.getPrecision();
        var recall = results.getRecall();
        var f1 = results.getF1();

        Assertions.assertAll(//
                () -> Assertions.assertTrue(precision >= MIN_PREC_TEASTORE,
                        "Precision " + precision + " is below the expected minimum value " + MIN_PREC_TEASTORE), //
                () -> Assertions.assertTrue(recall >= MIN_REC_TEASTORE, "Recall " + recall + " is below the expected minimum value " + MIN_REC_TEASTORE), //
                () -> Assertions.assertTrue(f1 >= MIN_F1_TEASTORE, "F1 " + f1 + " is below the expected minimum value " + MIN_F1_TEASTORE));
    }

    private static EvaluationResult evalInconsistency(Project project) {
        var name = project.name();
        logger.info("Starting Inconsistency Analyses for {}", name);
        DeleteOneModelElementEval eval1 = new DeleteOneModelElementEval();

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
        os.println("START Eval: " + project + " -- " + eval);

        IModelConnector pcmModel = project.getModel();
        IText annotatedText = project.getText();

        GoldStandard gs = project.getGoldStandard(pcmModel);
        var results = eval.evaluate(project, pcmModel, annotatedText, gs, os);

        os.println("END Eval: " + project + " -- " + eval);
        os.println("####################################\n");
        return results;
    }
}
