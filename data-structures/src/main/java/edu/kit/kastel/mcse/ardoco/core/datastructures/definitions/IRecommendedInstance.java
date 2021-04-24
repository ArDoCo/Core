package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface IRecommendedInstance {

    IRecommendedInstance createCopy();

    /**
     * Returns the involved name mappings.
     *
     * @return the name mappings of this recommended instance
     */
    List<INounMapping> getNameMappings();

    /**
     * Returns the involved type mappings.
     *
     * @return the type mappings of this recommended instance
     */
    List<INounMapping> getTypeMappings();

    /**
     * Returns the probability being an instance of the model.
     *
     * @return the probability to be found in the model
     */
    double getProbability();

    /**
     * Removes nameMappings from this recommended instance.
     *
     * @param nameMappings the name mappings to remove
     */
    void removeNounNodeMappingsFromName(List<INounMapping> nameMappings);

    /**
     * Adds a name and type mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     * @param typeMapping the type mapping to add
     */
    void addMappings(INounMapping nameMapping, INounMapping typeMapping);

    /**
     * Adds name and type mappings to this recommended instance.
     *
     * @param nameMapping the name mappings to add
     * @param typeMapping the type mappings to add
     */
    void addMappings(List<INounMapping> nameMapping, List<INounMapping> typeMapping);

    /**
     * Adds a name mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     */
    void addName(INounMapping nameMapping);

    /**
     * Adds a type mapping to this recommended instance.
     *
     * @param typeMapping the type mapping to add
     */
    void addType(INounMapping typeMapping);

    /**
     * Sets the probability to a given probability.
     *
     * @param probability the new probability
     */
    void setProbability(double probability);

    /**
     * Returns the type as string from this recommended instance.
     *
     * @return the type as string
     */
    String getType();

    /**
     * Returns the name as string from this recommended instance.
     *
     * @return the name as string
     */
    String getName();

    /**
     * Sets the type of this recommended instance to the given type.
     *
     * @param type the new type
     */
    void setType(String type);

    /**
     * Sets the name of this recommended instance to the given name.
     *
     * @param name the new name
     */
    void setName(String name);

}
