package edu.kit.ipd.consistency_analyzer.modules;

public interface IModule<T> {

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
    T getState();

}
