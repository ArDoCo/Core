package edu.kit.kastel.mcse.ardoco.tests;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunnerExt;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class TestRunner extends ArDoCoRunnerExt<TestRunner.Parameters> {
    public TestRunner(String projectName) {
        super(projectName);
    }

    public record Parameters(DiagramProject diagramProject, File outputDir, boolean useMockDiagrams) {
    }

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    @Override
    public boolean setUp(Parameters p) {
        try {
            definePipeline(p);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return false;
        }
        setOutputDirectory(p.outputDir);
        isSetUp = true;
        return true;
    }

    private void definePipeline(Parameters p) throws IOException {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        arDoCo.addPipelineStep(DiagramConnectionGenerator.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
    }
}
