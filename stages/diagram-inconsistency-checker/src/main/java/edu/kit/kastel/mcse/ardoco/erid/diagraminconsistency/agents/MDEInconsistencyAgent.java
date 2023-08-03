package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.MDEInconsistencyInformant;

public class MDEInconsistencyAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public MDEInconsistencyAgent(DataRepository dataRepository) {
        super(MDEInconsistencyAgent.class.getSimpleName(), dataRepository, List.of(new MDEInconsistencyInformant(dataRepository)));
        enabledInformants = getInformants().stream().map(Informant::getId).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
