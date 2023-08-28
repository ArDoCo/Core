/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.configuration.ChildClassConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This abstract class represents an execution step within ArDoCo. Examples are Text-Extraction, Recommendation-Generator, Connection-Generator, and
 * Inconsistency-Checker.
 * <p>
 * Implementing classes need to implement {@link #initializeState()} that cares for setting up the state for processing.
 */
public abstract class AbstractExecutionStage extends Pipeline {

    private final MutableList<PipelineAgent> agents;

    @Configurable
    @ChildClassConfigurable
    private List<String> enabledAgents;

    /**
     * Constructor for ExecutionStages
     * 
     * @param agents         the agents that could be executed by this pipeline
     * @param id             the id of the stage
     * @param dataRepository the {@link DataRepository} that should be used
     */
    protected AbstractExecutionStage(List<? extends PipelineAgent> agents, String id, DataRepository dataRepository) {
        super(id, dataRepository);
        this.agents = Lists.mutable.withAll(agents);
        this.enabledAgents = this.agents.collect(PipelineAgent::getId);
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

        for (var agent : agents) {
            if (enabledAgents.contains(agent.getId())) {
                this.addPipelineStep(agent);
            }
        }
    }

    /**
     * Prepare processing and set up the (internal) state
     */
    protected abstract void initializeState();

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }
}
