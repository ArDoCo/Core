/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;

/**
 * The Interface IRecommendedInstance defines the aggregation of noun mappings to one recommendation.
 */
public interface IRecommendedInstance extends ICopyable<IRecommendedInstance> {

    /**
     * Returns the involved name mappings.
     *
     * @return the name mappings of this recommended instance
     */
    ImmutableList<INounMapping> getNameMappings();

    /**
     * Returns the involved type mappings.
     *
     * @return the type mappings of this recommended instance
     */
    ImmutableList<INounMapping> getTypeMappings();

    /**
     * Returns the probability being an instance of the model.
     *
     * @return the probability to be found in the model
     */
    double getProbability();

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
    void addMappings(ImmutableList<INounMapping> nameMapping, ImmutableList<INounMapping> typeMapping);

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

    void addProbability(IClaimant claimant, double probability);

    Confidence getConfidencesForClaimant(IClaimant claimant);
}
