package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.DiagramAsModelInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.DiagramTextInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InitialDiagramConnectionAgent extends PipelineAgent {
    private static final String id = InitialDiagramConnectionAgent.class.getSimpleName();

    @Configurable
    private final List<String> enabledInformants;

    public InitialDiagramConnectionAgent(DataRepository dataRepository) {
        super(id, dataRepository, List.of(new DiagramAsModelInformant(dataRepository), new DiagramTextInformant(dataRepository)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
