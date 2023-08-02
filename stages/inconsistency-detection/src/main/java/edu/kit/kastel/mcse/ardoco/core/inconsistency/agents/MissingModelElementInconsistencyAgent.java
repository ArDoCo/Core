/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.MissingModelElementInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class MissingModelElementInconsistencyAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public MissingModelElementInconsistencyAgent(DataRepository dataRepository) {
        super(MissingModelElementInconsistencyAgent.class.getSimpleName(), dataRepository,
                List.of(new MissingModelElementInconsistencyInformant(dataRepository)));
        enabledInformants = getInformants().stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
