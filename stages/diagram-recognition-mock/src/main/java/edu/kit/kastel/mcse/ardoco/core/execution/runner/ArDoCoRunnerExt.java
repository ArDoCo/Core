package edu.kit.kastel.mcse.ardoco.core.execution.runner;

public abstract class ArDoCoRunnerExt<T extends Record> extends ArDoCoRunner {
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
}
