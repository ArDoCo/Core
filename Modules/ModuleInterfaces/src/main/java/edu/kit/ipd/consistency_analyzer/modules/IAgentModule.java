package edu.kit.ipd.consistency_analyzer.modules;

public interface IAgentModule<AgentDatastructure> extends IModule<AgentDatastructure> {

    /**
     * Runs the agents.
     */
    void runAgents();

}
