package edu.kit.kastel.mcse.ardoco.core.execution;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;

/**
 * Contains metadata about the pipeline which produced this data.
 */
public class PipelineMetaData implements PipelineStepData {
    public final static String ID = "PipelineMetaData";
    private final ArDoCoRunner runner;

    /**
     * Constructs a new PipelineMetaData with the given meta data
     *
     * @param runner the runner which produced the {@link edu.kit.kastel.mcse.ardoco.core.data.DataRepository DataRepository} this data is associated with
     */
    public PipelineMetaData(ArDoCoRunner runner) {
        this.runner = runner;
    }

    /**
     * {@return the runner which produced the DataRepository this data is associated with}
     */
    public ArDoCoRunner getRunner() {
        return this.runner;
    }
}
