package edu.kit.ipd.consistency_analyzer.modules;

public interface IAgentModule<T> extends IModule<T> {

    /**
     * Runs the agents.
     */
    void runAgents();

}
