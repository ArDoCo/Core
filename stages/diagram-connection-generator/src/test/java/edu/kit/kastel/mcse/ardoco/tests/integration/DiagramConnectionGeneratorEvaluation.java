package edu.kit.kastel.mcse.ardoco.tests.integration;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tests.PreTestRunner;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import java.io.File;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
public class DiagramConnectionGeneratorEvaluation extends DiagramConnectionGeneratorTest {
    private static final Logger logger =
            LoggerFactory.getLogger(DiagramConnectionGeneratorEvaluation.class);
    private static final String OUTPUT_DIR = "src/test/resources/testout";

    @Override
    protected DataRepository runPreTestRunner(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        var params = new PreTestRunner.Parameters(project, new File(OUTPUT_DIR), false);
        var runner = new PreTestRunner(project.name(), params);
        return runner.runWithoutSaving();
    }

    @Disabled
    @Test
    @Override
    protected void teammatesTest() {
        runComparable(DiagramProject.TEAMMATES, false);
    }

    @Disabled
    @Test
    @Override
    protected void teammatesHistTest() {
        runComparable(DiagramProject.TEAMMATES_HISTORICAL, false);
    }

    @Disabled
    @Test
    @Override
    protected void teastoreTest() {
        runComparable(DiagramProject.TEASTORE, false);
    }

    @Disabled
    @Test
    @Override
    protected void teastoreHistTest() {
        runComparable(DiagramProject.TEASTORE_HISTORICAL, false);
    }

    @Disabled
    @Test
    @Override
    protected void bbbTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON, false);
    }

    @Disabled
    @Test
    @Override
    protected void bbbHistTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON_HISTORICAL, false);
    }

    @Disabled
    @Test
    @Override
    protected void msTest() {
        runComparable(DiagramProject.MEDIASTORE, false);
    }
}
