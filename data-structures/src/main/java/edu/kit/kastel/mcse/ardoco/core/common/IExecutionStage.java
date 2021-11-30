/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.common;


import java.util.Map;

/**
 * The Interface IExecutionStage defines one execution stage of the approach.
 */
public interface IExecutionStage {
    /**
     * Runs the agent with its analyzers and extractors.
     */
    void exec();

    /**
     * Returns the current data structure.
     *
     * @return current data structure
     */
    AgentDatastructure getBlackboard();

    /**
     * Creates the module.
     *
     * @param data    the data
     * @param configs the configurations
     * @return the new module
     */
    IExecutionStage create(AgentDatastructure data, Map<String, String> configs);
}
