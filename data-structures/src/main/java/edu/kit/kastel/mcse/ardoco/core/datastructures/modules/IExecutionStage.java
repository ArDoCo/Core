package edu.kit.kastel.mcse.ardoco.core.datastructures.modules;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;

/**
 * The Interface IExecutionStage defines one execution stage of the approach.
 */
public interface IExecutionStage {
    /**
     * Runs the agent with its analyzers and finders.
     */
    void exec();

    /**
     * Returns the current data structure.
     *
     * @return current data structure
     */
    AgentDatastructure getState();

    /**
     * Creates the module.
     *
     * @param data    the data
     * @param configs the configurations
     * @return the new module
     */
    IExecutionStage create(AgentDatastructure data, Map<String, String> configs);
}
