package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AnonymousRunner extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ParameterizedRunner.class);

    protected AnonymousRunner(String projectName) {
        super(projectName);
        setUp();
    }

    /**
     * Sets up the runner. {@link #isSetUp} must return true, if successful.
     *
     * @return True on success, else false
     */
    public boolean setUp() {
        try {
            initializePipelineSteps();
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return false;
        }
        isSetUp = true;
        return true;
    }

    /**
     * Defines the pipeline according to the supplied parameters
     *
     * @throws IOException can occur when loading data
     */
    public abstract void initializePipelineSteps() throws IOException;
}
