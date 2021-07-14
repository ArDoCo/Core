package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;

public abstract class Extractor implements IExtractor {

    public abstract Extractor create(AgentDatastructure data, Configuration config);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
