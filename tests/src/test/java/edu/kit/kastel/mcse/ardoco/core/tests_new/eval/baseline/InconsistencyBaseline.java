/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.baseline;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.baseline.InconsistencyBaselineAgent;

import java.util.List;

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
