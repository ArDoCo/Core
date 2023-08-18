package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.agents.DiagramReferenceAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.agents.InitialDiagramConnectionAgent;

public class DiagramConnectionGenerator extends ExecutionStage {
    @Configurable
    private List<String> enabledAgents;

    public DiagramConnectionGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        super("DiagramConnectionGenerator", dataRepository,
                List.of(new DiagramReferenceAgent(dataRepository), new InitialDiagramConnectionAgent(dataRepository)), additionalConfigs);
        enabledAgents = getAgents().stream().map(Agent::getId).toList();
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramConnectionGenerator States");
        var diagramConnectionStates = new DiagramConnectionStatesImpl();
        getDataRepository().addData(DiagramConnectionStates.ID, diagramConnectionStates);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, getAgents());
    }
}
