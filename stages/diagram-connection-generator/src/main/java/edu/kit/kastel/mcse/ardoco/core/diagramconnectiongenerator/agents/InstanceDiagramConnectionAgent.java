package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.informants.BaseDiagramConnectionInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InstanceDiagramConnectionAgent extends PipelineAgent {
    private final List<Informant> informants;

    @Configurable
    private final List<String> enabledInformants;

    public InstanceDiagramConnectionAgent(DataRepository dataRepository) {
        super(InstanceDiagramConnectionAgent.class.getSimpleName(), dataRepository);

        informants = List.of(new BaseDiagramConnectionInformant(dataRepository));
        enabledInformants = informants.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
