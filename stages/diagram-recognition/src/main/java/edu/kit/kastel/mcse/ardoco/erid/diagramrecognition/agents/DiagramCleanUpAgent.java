package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramTextBoxCleanUpInformant;

public class DiagramCleanUpAgent extends PipelineAgent {

    public DiagramCleanUpAgent(DataRepository dataRepository) {
        super(List.of(new DiagramTextBoxCleanUpInformant(dataRepository)), DiagramCleanUpAgent.class.getSimpleName(), dataRepository);
    }
}
