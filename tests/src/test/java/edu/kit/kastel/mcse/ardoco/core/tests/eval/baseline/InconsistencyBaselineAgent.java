/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * Agent for {@link InconsistencyBaseline}
 */
public class InconsistencyBaselineAgent extends PipelineAgent {

    protected InconsistencyBaselineAgent(DataRepository dataRepository) {
        super(InconsistencyBaselineAgent.class.getSimpleName(), dataRepository);
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return List.of(new InconsistencyBaselineInformant(getDataRepository()));
    }
}
