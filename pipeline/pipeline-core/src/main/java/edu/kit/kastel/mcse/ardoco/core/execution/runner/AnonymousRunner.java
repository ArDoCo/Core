package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import edu.kit.kastel.mcse.ardoco.core.execution.PipelineMetaData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

import java.io.IOException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AnonymousRunner extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(AnonymousRunner.class);

    private final List<AbstractPipelineStep> pipelineSteps;

    protected AnonymousRunner(String projectName) {
        super(projectName);
        pipelineSteps = setUp();
    }

    /**
     * Sets up the runner using {@link #initializePipelineSteps}. {@link #isSetUp} must return true, if successful.
     *
     * @return List of AbstractPipelineSteps this runner consists of
     */
    public List<AbstractPipelineStep> setUp() {
        try {
            var arDoCo = getArDoCo();
            var pipelineSteps = initializePipelineSteps();
            pipelineSteps.forEach(arDoCo::addPipelineStep);
            arDoCo.getDataRepository().addData(PipelineMetaData.ID, new PipelineMetaData(this));
            isSetUp = true;
            return pipelineSteps;
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return List.of();
        }
    }

    /**
     * Initializes and returns the pipeline steps according to the supplied parameters
     *
     * @throws IOException can occur when loading data
     */
    public abstract List<AbstractPipelineStep> initializePipelineSteps() throws IOException;
}
