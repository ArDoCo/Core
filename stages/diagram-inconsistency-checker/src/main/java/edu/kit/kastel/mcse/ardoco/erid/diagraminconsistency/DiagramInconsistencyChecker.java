package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents.RecommendedInstancesConfidenceAgent;

public class DiagramInconsistencyChecker extends ExecutionStage {
    @Configurable
    private List<String> enabledAgents;

    public DiagramInconsistencyChecker(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        super(DiagramInconsistencyChecker.class.getSimpleName(), dataRepository,
                List.of(new InconsistencyAgent(dataRepository), new RecommendedInstancesConfidenceAgent(dataRepository)), additionalConfigs);
        enabledAgents = getAgents().stream().map(Agent::getId).toList();
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramInconsistency States");
        var diagramInconsistencyStates = new DiagramInconsistencyStatesImpl();
        getDataRepository().addData(DiagramInconsistencyStates.ID, diagramInconsistencyStates);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, getAgents());
    }
}
