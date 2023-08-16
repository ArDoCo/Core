package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.AbbreviationInformant;

public class AbbreviationAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public AbbreviationAgent(DataRepository dataRepository) {
        super(AbbreviationAgent.class.getSimpleName(), dataRepository, List.of(new AbbreviationInformant(dataRepository)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
