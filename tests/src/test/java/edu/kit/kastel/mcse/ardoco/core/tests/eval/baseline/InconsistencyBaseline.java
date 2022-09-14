/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline;

import java.util.List;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;

/**
 * Baseline approach for inconsistency detection
 */
public class InconsistencyBaseline extends AbstractExecutionStage {

    public InconsistencyBaseline(DataRepository dataRepository) {
        super("InconsistencyBaseline", dataRepository);
    }

    @Override
    protected void initializeState() {
        // do nothing
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return List.of(new InconsistencyBaselineAgent(getDataRepository()));
    }
}
