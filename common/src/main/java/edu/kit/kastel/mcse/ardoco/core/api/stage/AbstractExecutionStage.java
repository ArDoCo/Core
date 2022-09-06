/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.stage;

import java.util.List;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;

/**
 * This abstract class represents an execution step within ArDoCo. Examples are Text-Extraction, Recommendation-Generator, Connection-Generator, and
 * Inconsistency-Checker.
 *
 * Implementing classes need to implement {@link #initializeState()} that cares for setting up the state for processing.
 * Additionally, implementing classes need to implement {@link #getEnabledAgents()} that returns the listof enabled {@link PipelineAgent pipeline agents}
 */
public abstract class AbstractExecutionStage extends Pipeline {

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
