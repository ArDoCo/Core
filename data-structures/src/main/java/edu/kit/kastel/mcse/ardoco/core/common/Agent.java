/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Defines the base class of all agents.
 */
public abstract class Agent implements IAgent {

    /** The logger. */
    protected final Logger logger = LogManager.getLogger(this.getClass());
    private final Class<? extends Configuration> configType;

    /**
     * Use for prototype constructors.
     *
     * @param configType the desired type of configurations for {@link #create(AgentDatastructure, Configuration)}
     */
    protected Agent(Class<? extends Configuration> configType) {
        this.configType = Objects.requireNonNull(configType);
    }

    /**
     * Create an agent instance based on this prototype.
     *
     * @param data   the blackboard to be used
     * @param config the configuration for the agent
     * @return the agent instance
     * @throws IllegalArgumentException iff the config has not the class specified in {@link #Agent(Class)}
     */
    public final Agent create(AgentDatastructure data, Configuration config) {
        if (!configType.isInstance(config)) {
            throw new IllegalArgumentException(String.format("Configuration invalid: Expected: %s - Present: %s", configType, config.getClass()));
        }
        return createInternal(data, config);
    }

    /**
     * Create the agent. It is guaranteed that config is an instance of {@link #configType}. This is the secret
     * implementation of {@link #create(AgentDatastructure, Configuration)}.
     *
     * @param data   the data structure
     * @param config the configuration
     * @return the new agent
     */
    protected abstract Agent createInternal(AgentDatastructure data, Configuration config);

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

}
