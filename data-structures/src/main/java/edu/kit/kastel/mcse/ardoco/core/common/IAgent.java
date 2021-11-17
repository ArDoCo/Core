package edu.kit.kastel.mcse.ardoco.core.common;

/**
 * This interface defines the behaviour of all agents.
 */
public interface IAgent extends ILoadable {

    /**
     * Execute the agent.
     */
    void exec();
}
