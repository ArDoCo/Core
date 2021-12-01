/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;

/**
 * This class represents recommended instances. These instances should be contained by the model. The likelihood is
 * measured by the probability. Every recommended instance has a unique name.
 *
 * @author Sophie
 *
 */
public class RecommendedInstance implements IRecommendedInstance {

    private String type;
    private String name;
    private double probability;
    private Set<INounMapping> typeMappings;
    private Set<INounMapping> nameMappings;

    @Override
    public IRecommendedInstance createCopy() {
        return new RecommendedInstance(name, type, probability, Lists.immutable.fromStream(nameMappings.stream().map(INounMapping::createCopy)),
                Lists.immutable.fromStream(typeMappings.stream().map(INounMapping::createCopy)));
    }

    /**
     * Creates a new recommended instance.
     *
     * @param name        the name of the instance
     * @param type        the type of the instance
     * @param probability the probability that this instance should be found in the model
     * @param nameNodes   the involved name mappings
     * @param typeNodes   the involved type mappings
     */
    public RecommendedInstance(String name, String type, double probability, ImmutableList<INounMapping> nameNodes, ImmutableList<INounMapping> typeNodes) {
        this.type = type;
        this.name = name;
        this.probability = probability;
        nameMappings = new HashSet<>(nameNodes.castToCollection());
        typeMappings = new HashSet<>(typeNodes.castToCollection());
    }

    /**
     * Returns the involved name mappings.
     *
     * @return the name mappings of this recommended instance
     */
    @Override
    public ImmutableList<INounMapping> getNameMappings() {
        return Lists.immutable.withAll(nameMappings);
    }

    /**
     * Returns the involved type mappings.
     *
     * @return the type mappings of this recommended instance
     */
    @Override
    public ImmutableList<INounMapping> getTypeMappings() {
        return Lists.immutable.withAll(typeMappings);
    }

    /**
     * Returns the probability being an instance of the model.
     *
     * @return the probability to be found in the model
     */
    @Override
    public double getProbability() {
        return probability;
    }

    /**
     * Removes nameMappings from this recommended instance.
     *
     * @param nameMappings the name mappings to remove
     */
    @Override
    public void removeNounNodeMappingsFromName(ImmutableList<INounMapping> nameMappings) {
        this.nameMappings.removeAll(nameMappings.castToCollection());
    }

    /**
     * Adds a name and type mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     * @param typeMapping the type mapping to add
     */
    @Override
    public void addMappings(INounMapping nameMapping, INounMapping typeMapping) {
        addName(nameMapping);
        addType(typeMapping);
    }

    /**
     * Adds name and type mappings to this recommended instance.
     *
     * @param nameMapping the name mappings to add
     * @param typeMapping the type mappings to add
     */
    @Override
    public void addMappings(ImmutableList<INounMapping> nameMapping, ImmutableList<INounMapping> typeMapping) {
        nameMapping.forEach(this::addName);
        typeMapping.forEach(this::addType);
    }

    /**
     * Adds a name mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     */
    @Override
    public void addName(INounMapping nameMapping) {
        nameMappings.add(nameMapping);
    }

    /**
     * Adds a type mapping to this recommended instance.
     *
     * @param typeMapping the type mapping to add
     */
    @Override
    public void addType(INounMapping typeMapping) {
        typeMappings.add(typeMapping);
    }

    /**
     * Sets the probability to a given probability.
     *
     * @param probability the new probability
     */
    @Override
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Returns the type as string from this recommended instance.
     *
     * @return the type as string
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Returns the name as string from this recommended instance.
     *
     * @return the name as string
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the type of this recommended instance to the given type.
     *
     * @param type the new type
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the name of this recommended instance to the given name.
     *
     * @param name the new name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        var separator = "\n\t\t\t\t\t";
        MutableList<String> typeNodeVals = Lists.mutable.empty();
        MutableList<String> typeOccurrences = Lists.mutable.empty();
        MutableList<Integer> typePositions = Lists.mutable.empty();
        for (INounMapping typeMapping : typeMappings) {
            typeNodeVals.add(typeMapping.toString());
            typeOccurrences.addAll(typeMapping.getSurfaceForms().castToCollection());
            typePositions.addAll(typeMapping.getMappingSentenceNo().castToCollection());
        }

        MutableList<String> nameNodeVals = Lists.mutable.empty();
        MutableList<String> nameOccurrences = Lists.mutable.empty();
        MutableList<Integer> namePositions = Lists.mutable.empty();
        for (INounMapping nameMapping : nameMappings) {
            nameNodeVals.add(nameMapping.toString());
            nameOccurrences.addAll(nameMapping.getSurfaceForms().castToCollection());
            namePositions.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }
        return "RecommendationInstance [" + " name=" + name + ", type=" + type + ", probability=" + probability + //
                ", mappings:]= " + separator + String.join(separator, nameNodeVals) + separator + String.join(separator, typeNodeVals) + "\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RecommendedInstance other = (RecommendedInstance) obj;
        return Objects.equals(name, other.name) && Objects.equals(type, other.type);
    }

}
