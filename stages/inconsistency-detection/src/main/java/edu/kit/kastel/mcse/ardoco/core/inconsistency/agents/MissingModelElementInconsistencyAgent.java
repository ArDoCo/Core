/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.MissingModelElementInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class MissingModelElementInconsistencyAgent extends PipelineAgent {
    public MissingModelElementInconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new MissingModelElementInconsistencyInformant(dataRepository)), MissingModelElementInconsistencyAgent.class.getSimpleName(),
                dataRepository);
    }
}
