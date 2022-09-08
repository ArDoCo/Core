/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;

/**
 * The Interface IRecommendedInstance defines the aggregation of noun mappings to one recommendation.
 */
public interface RecommendedInstance {

    /**
     * Returns the involved name mappings.
     *
     * @return the name mappings of this recommended instance
     */
    ImmutableList<NounMapping> getNameMappings();

    /**
     * Returns the involved type mappings.
     *
     * @return the type mappings of this recommended instance
     */
    ImmutableList<NounMapping> getTypeMappings();

    /**
     * Returns the probability being an instance of the model.
     *
     * @return the probability to be found in the model
     */
    double getProbability();

    /**
     * Adds a probability to the recommended instance
     *
     * @param claimant    the claimant of the confidence
     * @param probability the confidence
     */
    void addProbability(Claimant claimant, double probability);

    /**
     * Adds a name and type mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     * @param typeMapping the type mapping to add
     */
    void addMappings(NounMapping nameMapping, NounMapping typeMapping);

    /**
     * Adds name and type mappings to this recommended instance.
     *
     * @param nameMapping the name mappings to add
     * @param typeMapping the type mappings to add
     */
    void addMappings(ImmutableList<NounMapping> nameMapping, ImmutableList<NounMapping> typeMapping);

    /**
     * Adds a name mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     */
    void addName(NounMapping nameMapping);

    /**
     * Adds a type mapping to this recommended instance.
     *
     * @param typeMapping the type mapping to add
     */
    void addType(NounMapping typeMapping);

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

    ImmutableSortedSet<Integer> getSentenceNumbers();

    ImmutableSet<Claimant> getClaimants();
}
