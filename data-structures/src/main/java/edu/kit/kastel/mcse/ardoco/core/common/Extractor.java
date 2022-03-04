/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.common;

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
    public abstract Extractor create(String modelId, AgentDatastructure data, Configuration config);

    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

}
