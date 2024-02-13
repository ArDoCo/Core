/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tests.PreTestRunner;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

/**
 * This class is used for evaluating ERID's diagram-to-sentences TLR capabilities using the automatically extracted diagrams
 * ({@link edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition DiagramRecognition}).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiagramConnectionGeneratorEvaluationIT extends DiagramConnectionGeneratorTest {
    private static final Logger logger = LoggerFactory.getLogger(DiagramConnectionGeneratorEvaluationIT.class);
    private static final String OUTPUT_DIR = "src/test/resources/testout";

    @Override
    protected ExpectedResults getExpectedResults(DiagramProject project) {
        return project.getExpectedDiagramSentenceTlrResults();
    }

    @Override
    protected DataRepository runPreTestRunner(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        var params = new PreTestRunner.Parameters(project, new File(OUTPUT_DIR), false);
        var runner = new PreTestRunner(project.name(), params);
        return runner.runWithoutSaving();
    }

    @Override
    @Test
    @Disabled
    void evaluateAll() {
        super.evaluateAll();
    }

    @Override
    @Test
    @Disabled
    void teammatesTest() {
        runComparable(DiagramProject.TEAMMATES, false);
    }

    @Override
    @Test
    @Disabled
    void teammatesHistTest() {
        runComparable(DiagramProject.TEAMMATES_HISTORICAL, false);
    }

    @Override
    @Test
    @Disabled
    void teastoreTest() {
        runComparable(DiagramProject.TEASTORE, false);
    }

    @Override
    @Test
    @Disabled
    void teastoreHistTest() {
        runComparable(DiagramProject.TEASTORE_HISTORICAL, false);
    }

    @Override
    @Test
    @Disabled
    void bbbTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON, false);
    }

    @Override
    @Test
    @Disabled
    void bbbHistTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON_HISTORICAL, false);
    }

    @Override
    @Test
    @Disabled
    void msTest() {
        runComparable(DiagramProject.MEDIASTORE, false);
    }
}
