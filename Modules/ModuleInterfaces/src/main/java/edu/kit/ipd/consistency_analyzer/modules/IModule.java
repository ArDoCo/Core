package edu.kit.ipd.consistency_analyzer.modules;

public interface IModule<AgentDatastructure> {

    // TODO: generic defintion of InputStates

    /**
     * Runs the agent with its analyzers and finders.
     */
    void exec();

    /**
     * Returns the current state.
     *
     * @return current state
     */
    AgentDatastructure getState();

}
