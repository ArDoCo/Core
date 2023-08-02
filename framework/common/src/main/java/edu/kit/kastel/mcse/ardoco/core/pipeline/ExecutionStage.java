package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public abstract class ExecutionStage extends AbstractExecutionStage {
    /**
     * Creates an {@link ExecutionStage} and applies the additional configuration to it
     *
     * @param id                the id of the stage
     * @param dataRepository    the {@link DataRepository} that should be used
     * @param agents            the pipeline agents this stage supports
     * @param additionalConfigs the additional configuration
     */
    protected ExecutionStage(String id, DataRepository dataRepository, List<PipelineAgent> agents, Map<String, String> additionalConfigs) {
        super(id, dataRepository, agents);
        applyConfiguration(additionalConfigs);
    }
}
