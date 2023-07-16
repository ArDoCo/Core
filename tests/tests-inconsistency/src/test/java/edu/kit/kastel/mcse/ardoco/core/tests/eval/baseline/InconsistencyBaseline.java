/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * Baseline approach for inconsistency detection
 */
public class InconsistencyBaseline extends AbstractExecutionStage {
    private PipelineAgent agent;

    public InconsistencyBaseline(DataRepository dataRepository) {
        super("InconsistencyBaseline", dataRepository);
        agent = new InconsistencyBaselineAgent(dataRepository);
    }

    @Override
    protected void initializeState() {
        // do nothing
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return List.of(agent);
    }

    @Override
    public List<PipelineAgent> getAgents() {
        return List.of(agent);
    }
}
