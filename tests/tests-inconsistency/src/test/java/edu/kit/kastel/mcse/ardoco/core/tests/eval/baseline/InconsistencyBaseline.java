/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

/**
 * Baseline approach for inconsistency detection
 */
public class InconsistencyBaseline extends AbstractExecutionStage {
    private PipelineAgent agent;

    public InconsistencyBaseline(DataRepository dataRepository) {
        super(List.of(new InconsistencyBaselineAgent(dataRepository)), "InconsistencyBaseline", dataRepository);
    }

    @Override
    protected void initializeState() {
        // do nothing
    }
}
