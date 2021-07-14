package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import java.util.Objects;
import java.util.logging.Logger;

public abstract class Agent implements IAgent {
    protected final Logger logger = Logger.getLogger(getName());
    private final Class<? extends Configuration> configType;

    /**
     * Prototype Constructor
     */
    protected Agent(Class<? extends Configuration> configType) {
        this.configType = Objects.requireNonNull(configType);
    }

    public final Agent create(AgentDatastructure data, Configuration config) {
        if (!configType.isInstance(config)) {
            throw new IllegalArgumentException(String.format("Configuration invalid: Expected: %s - Present: %s", configType, config.getClass()));
        }
        return createInternal(data, config);
    }

    /**
     * Create the agent. It is guaranteed that config is an instance of {@link #configType}.
     *
     * @param data   the data structure
     * @param config the configuration
     * @return the new agent
     */
    protected abstract Agent createInternal(AgentDatastructure data, Configuration config);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
