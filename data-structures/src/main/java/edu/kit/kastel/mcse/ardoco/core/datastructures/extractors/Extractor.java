package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;

/**
 * The Class Extractor defines the base type of all special extractors.
 */
public abstract class Extractor implements IExtractor {

    /**
     * Creates the extractor.
     *
     * @param data   the data
     * @param config the config
     * @return the extractor
     */
    public abstract Extractor create(AgentDatastructure data, Configuration config);

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

}
