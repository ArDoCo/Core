package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramModelReferenceInformant;

public class DiagramReferenceAgent extends PipelineAgent {
    @Configurable
    private final List<String> enabledInformants;

    public DiagramReferenceAgent(DataRepository dataRepository) {
        super(DiagramReferenceAgent.class.getSimpleName(), dataRepository, List.of(new DiagramModelReferenceInformant(dataRepository)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
