package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;

public abstract class ArDoCoRunnerExt<T extends Record> extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoRunnerExt.class);

    protected ArDoCoRunnerExt(String projectName) {
        super(projectName);
    }

    /**
     * Sets up the runner. {@link #isSetUp} must return true, if successful.
     *
     * @param parameters Contains the parameters used during setup
     * @return True on success, else false
     */
    public abstract boolean setUp(T parameters);

    /**
     * TODO Remove, see 28.06. notes about NoSuchElementException
     */
    public final void runWithoutSaving() {
        if (this.isSetUp()) {
            this.getArDoCo().run();
        } else {
            logger.error("Cannot run ArDoCo because the runner is not properly set up.");
        }
    }

    @Override
    public ArDoCo getArDoCo() {
        return super.getArDoCo();
    }
}
