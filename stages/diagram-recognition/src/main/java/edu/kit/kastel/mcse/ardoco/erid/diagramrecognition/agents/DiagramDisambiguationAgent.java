package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramDisambiguationInformant;

public class DiagramDisambiguationAgent extends PipelineAgent {
    public DiagramDisambiguationAgent(DataRepository dataRepository) {
        super(List.of(new DiagramDisambiguationInformant(dataRepository)), DiagramDisambiguationAgent.class.getSimpleName(), dataRepository);
        ;
    }
}
