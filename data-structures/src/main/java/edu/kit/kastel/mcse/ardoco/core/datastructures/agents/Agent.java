package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import java.util.Objects;
import java.util.logging.Logger;

public abstract class Agent implements IAgent {
	protected final Logger logger = Logger.getLogger(getName());
	protected DependencyType dependencyType;
	private final Class<? extends Configuration> configType;

	/**
	 * Prototype Constructor
	 */
	protected Agent(Class<? extends Configuration> configType) {
		this.configType = Objects.requireNonNull(configType);
	}

	/**
	 * Creates a new agent of the specified type.
	 *
	 * @param type the agent type
	 */
	protected Agent(DependencyType dependencyType, Class<? extends Configuration> configType) {
		this.dependencyType = dependencyType;
		this.configType = configType;
	}

	public final Agent create(AgentDatastructure data, Configuration config) {
		if (!configType.isInstance(config)) {
			throw new IllegalArgumentException(String.format("Configuration invalid: Expected: %s - Present: %s", configType, config.getClass()));
		}
		return this.createInternal(data, config);
	}

	/**
	 * Create the agent. It is guaranteed that config is an instance of {@link #configType}.
	 *
	 * @param  data   the data structure
	 * @param  config the configuration
	 * @return        the new agent
	 */
	protected abstract Agent createInternal(AgentDatastructure data, Configuration config);

	/**
	 * Returns the dependency type of the current agent.
	 *
	 * @return the dependency type of the current agent
	 */
	public DependencyType getDependencyType() {
		return dependencyType;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
