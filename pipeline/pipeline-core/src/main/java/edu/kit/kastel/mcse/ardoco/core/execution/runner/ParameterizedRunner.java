/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

/**
 * An {@link ArDoCoRunner} that takes a record of type {@code <T>} to set itself up.
 *
 * @param <T> a record containing the parameters required to set up the runner
 */
public abstract class ParameterizedRunner<T extends Record> extends ArDoCoRunner implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ParameterizedRunner.class);

    protected ParameterizedRunner(String projectName, T parameters) {
        super(projectName);
        setUp(parameters);
    }

    /**
     * Sets up the runner using {@link #initializePipelineSteps}. {@link #isSetUp} must return true, if successful.
     *
     * @param p Contains the parameters used during setup
     * @return List of AbstractPipelineSteps this runner consists of
     */
    private List<AbstractPipelineStep> setUp(T p) {
        try {
            var arDoCo = getArDoCo();
            var pipelineSteps = initializePipelineSteps(p);
            pipelineSteps.forEach(arDoCo::addPipelineStep);
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
     * @param p the supplied parameters
     * @throws IOException can occur when loading data
     */
    public abstract List<AbstractPipelineStep> initializePipelineSteps(T p) throws IOException;
}
