/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import java.util.List;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;

/**
 * This class represents a pipeline agent that calculates some results for an {@link edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage execution
 * stage}.
 *
 * Implementing classes need to override {@link #getEnabledPipelineSteps()}.
 * Additionally, sub-classes are free to override {@link #initializeState()} to execute code at the beginning of the initialization before the main processing.
 */
public abstract class PipelineAgent extends Pipeline implements Agent {

    protected PipelineAgent(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    @Override
    protected final void preparePipelineSteps() {
        initialize();
        super.preparePipelineSteps();
    }

    /**
     * Initialize the execution
     */
    protected final void initialize() {
        initializeState();
        for (var informant : getEnabledPipelineSteps()) {
            this.addPipelineStep(informant);
        }
    }

    /**
     * If necessary, override this method to additionally initialize the state before the processing
     */
    protected void initializeState() {
        // do nothing here
    }

    /**
     * Return the enabled pipeline steps (informants)
     * 
     * @return the list of Informants
     */
    protected abstract List<Informant> getEnabledPipelineSteps();
}
