package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramModelReferenceInformant;

public class DiagramReferenceAgent extends PipelineAgent {
    public DiagramReferenceAgent(DataRepository dataRepository) {
        super(List.of(new DiagramModelReferenceInformant(dataRepository)), DiagramReferenceAgent.class.getSimpleName(), dataRepository);
    }
}
