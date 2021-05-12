package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.IAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IAgentModule;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.GenericInconsistencyConfig;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures.InconsistencyState;

public class InconsistencyChecker implements IAgentModule<AgentDatastructure> {

    private AgentDatastructure data;
    private List<IAgent> agents = new ArrayList<>();
    private InconsistencyCheckerConfig config;
    private GenericInconsistencyConfig agentConfig;

    public InconsistencyChecker(AgentDatastructure data) {
        this(data, InconsistencyCheckerConfig.DEFAULT_CONFIG, GenericInconsistencyConfig.DEFAULT_CONFIG);
    }

    public InconsistencyChecker(AgentDatastructure data, InconsistencyCheckerConfig config, GenericInconsistencyConfig agentConfig) {
        this.data = data;
        this.config = config;
        this.agentConfig = agentConfig;
        data.setInconsistencyState(new InconsistencyState());
        initializeAgents();
    }

    private void initializeAgents() {
        Map<String, InconsistencyAgent> myAgents = Loader.loadLoadable(InconsistencyAgent.class);

        for (String inconsistencyAgent : config.inconsistencyAgents) {
            if (!myAgents.containsKey(inconsistencyAgent)) {
                throw new IllegalArgumentException("InconsistencyAgent " + inconsistencyAgent + " not found");
            }
            agents.add(myAgents.get(inconsistencyAgent).create(data, agentConfig));
        }
    }

    @Override
    public void exec() {
        runAgents();
    }

    @Override
    public AgentDatastructure getState() {
        return data;
    }

    @Override
    public IModule<AgentDatastructure> create(AgentDatastructure data, Map<String, String> configs) {
        return new InconsistencyChecker(data, new InconsistencyCheckerConfig(configs), new GenericInconsistencyConfig(configs));
    }

    @Override
    public void runAgents() {
        for (IAgent agent : agents) {
            agent.exec();
        }
    }

}
