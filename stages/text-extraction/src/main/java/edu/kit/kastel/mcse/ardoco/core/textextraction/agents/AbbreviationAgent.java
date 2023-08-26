package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.AbbreviationInformant;

public class AbbreviationAgent extends PipelineAgent {
    public AbbreviationAgent(DataRepository dataRepository) {
        super(List.of(new AbbreviationInformant(dataRepository)), AbbreviationAgent.class.getSimpleName(), dataRepository);
    }
}
