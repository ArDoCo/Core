package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.informants.DiagramAsModelInformant;
import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.informants.DiagramTextInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InitialDiagramConnectionAgent extends PipelineAgent {
    private static final String id = InitialDiagramConnectionAgent.class.getSimpleName();

    private final List<Informant> informants;

    @Configurable
    private final List<String> enabledInformants;

    public InitialDiagramConnectionAgent(DataRepository dataRepository) {
        super(id, dataRepository);

        informants = List.of(new DiagramAsModelInformant(dataRepository), new DiagramTextInformant(dataRepository));
        enabledInformants = informants.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    public List<Informant> getPipelineSteps() {
        return List.copyOf(informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
