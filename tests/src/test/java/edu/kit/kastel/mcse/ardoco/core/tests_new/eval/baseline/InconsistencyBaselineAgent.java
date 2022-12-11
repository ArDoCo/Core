/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.baseline;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.baseline.InconsistencyBaselineInformant;

import java.util.List;

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
