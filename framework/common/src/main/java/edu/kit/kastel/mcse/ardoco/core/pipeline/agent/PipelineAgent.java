/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.agent;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * This class represents a pipeline agent that calculates some results for an {@link AbstractExecutionStage} execution
 * stage}.
 *
 * Implementing classes need to override {@link #getAllPipelineSteps()} and {@link #getEnabledPipelineStepIds()}.
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

        var enabledPipelineStepIds = getEnabledPipelineStepIds();

        for (var informant : getAllPipelineSteps()) {
            if (enabledPipelineStepIds.contains(informant.getId())) {
                this.addPipelineStep(informant);
            }
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
     * @return the list of informants ids
     */
    protected abstract List<String> getEnabledPipelineStepIds();

    /**
     * Return all possible pipeline steps (informants)
     *
     * @return the list of Informants
     */
    protected abstract List<Informant> getAllPipelineSteps();
}
