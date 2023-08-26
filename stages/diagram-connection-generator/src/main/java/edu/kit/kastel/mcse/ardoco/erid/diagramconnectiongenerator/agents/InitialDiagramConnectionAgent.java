package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.DiagramAsModelInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.DiagramTextInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.LinkBetweenDeAndRiProbabilityFilter;

public class InitialDiagramConnectionAgent extends PipelineAgent {
    public InitialDiagramConnectionAgent(DataRepository dataRepository) {
        super(List.of(new DiagramAsModelInformant(dataRepository), new DiagramTextInformant(dataRepository),
                new LinkBetweenDeAndRiProbabilityFilter(dataRepository)), InitialDiagramConnectionAgent.class.getSimpleName(), dataRepository);
    }
}
