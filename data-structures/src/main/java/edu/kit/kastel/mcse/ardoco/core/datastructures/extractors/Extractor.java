package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;

public abstract class Extractor implements IExtractor {

    protected DependencyType dependencyType;

    public abstract Extractor create(AgentDatastructure data, Configuration config);

    /**
     * Creates a new agent of the specified type.
     *
     * @param type the agent type
     */
    protected Extractor(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

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
