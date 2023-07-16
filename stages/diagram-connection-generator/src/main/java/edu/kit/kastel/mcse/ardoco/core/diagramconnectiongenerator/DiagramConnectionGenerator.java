package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.agents.InitialDiagramConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class DiagramConnectionGenerator extends AbstractExecutionStage {
    private final MutableList<PipelineAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    public DiagramConnectionGenerator(DataRepository dataRepository) {
        super("DiagramConnectionGenerator", dataRepository);
        agents = Lists.mutable.of(new InitialDiagramConnectionAgent(dataRepository));
        enabledAgents = agents.collect(Agent::getId);
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramConnectionGenerator States");
        var diagramConnectionStates = new DiagramConnectionStatesImpl();
        getDataRepository().addData(DiagramConnectionStates.ID, diagramConnectionStates);
    }

    public static DiagramConnectionGenerator get(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var diagramConnectionGenerator = new DiagramConnectionGenerator(dataRepository);
        diagramConnectionGenerator.applyConfiguration(additionalConfigs);
        return diagramConnectionGenerator;
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, agents);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }

    @Override
    public List<PipelineAgent> getAgents() { return List.copyOf(agents); }
}
