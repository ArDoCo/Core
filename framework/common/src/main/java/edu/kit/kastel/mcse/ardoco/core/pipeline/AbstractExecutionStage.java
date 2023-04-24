/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This abstract class represents an execution step within ArDoCo. Examples are Text-Extraction, Recommendation-Generator, Connection-Generator, and
 * Inconsistency-Checker.
 * <p>
 * Implementing classes need to implement {@link #initializeState()} that cares for setting up the state for processing.
 * Additionally, implementing classes need to implement {@link #getEnabledAgents()} that returns the listof enabled {@link PipelineAgent pipeline agents}
 */
public abstract class AbstractExecutionStage extends Pipeline {

    /**
     * Constructor for ExecutionStages
     *
     * @param id             the id of the stage
     * @param dataRepository the {@link DataRepository} that should be used
     */
    protected AbstractExecutionStage(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    @Override
    protected final void preparePipelineSteps() {
        initialize();
        super.preparePipelineSteps();
    }

    /**
     * Initialize the {@link AbstractExecutionStage}. Within this method, cares about all agents that should be executed by this pipeline
     */
    protected final void initialize() {
        initializeState();

        for (var agent : getEnabledAgents()) {
            this.addPipelineStep(agent);
        }
    }

    /**
     * Prepare processing and set up the (internal) state
     */
    protected abstract void initializeState();

    /**
     * Return the enabled {@link PipelineAgent agents}
     *
     * @return the list of agents
     */
    protected abstract List<PipelineAgent> getEnabledAgents();
}
