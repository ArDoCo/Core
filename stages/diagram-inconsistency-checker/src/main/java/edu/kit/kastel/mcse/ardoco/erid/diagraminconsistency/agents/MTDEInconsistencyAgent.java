package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.MTDEInconsistencyInformant;

public class MTDEInconsistencyAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public MTDEInconsistencyAgent(DataRepository dataRepository) {
        super(MTDEInconsistencyAgent.class.getSimpleName(), dataRepository, List.of(new MTDEInconsistencyInformant(dataRepository)));
        enabledInformants = getInformants().stream().map(Informant::getId).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
