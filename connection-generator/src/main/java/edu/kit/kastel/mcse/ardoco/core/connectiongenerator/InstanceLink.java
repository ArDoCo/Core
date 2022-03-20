/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;

/**
 * Represents a trace link between an instance of the extracted model and a recommended instance.
 *
 * @author Sophie
 */
public class InstanceLink implements IInstanceLink {

    private final IRecommendedInstance textualInstance;
    private final IModelInstance modelInstance;
    private double probability;

    @Override
    public IInstanceLink createCopy() {
        return new InstanceLink(textualInstance.createCopy(), modelInstance.createCopy(), probability);
    }

    /**
     * Creates a new instance link.
     *
     * @param textualInstance the recommended instance
     * @param modelInstance   the extracted instance
     * @param probability     the probability of this link
     */
    public InstanceLink(IRecommendedInstance textualInstance, IModelInstance modelInstance, double probability) {
        this.textualInstance = textualInstance;
        this.modelInstance = modelInstance;
        this.probability = probability;
    }

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    @Override
    public double getProbability() {
        return probability;
    }

    /**
     * Sets the probability to the given probability.
     *
     * @param probability the new probability
     */
    @Override
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Returns the recommended instance.
     *
     * @return the textual instance
     */
    @Override
    public IRecommendedInstance getTextualInstance() {
        return textualInstance;
    }

    /**
     * Returns the model instance.
     *
     * @return the extracted instance
     */
    @Override
    public IModelInstance getModelInstance() {
        return modelInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelInstance, textualInstance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InstanceLink other = (InstanceLink) obj;
        return Objects.equals(modelInstance, other.modelInstance) && Objects.equals(textualInstance, other.textualInstance);
    }

    /**
     * Returns all occurrences of all recommended instance names as string.
     *
     * @return all names of the recommended instances
     */
    @Override
    public String getNameOccurrencesAsString() {
        Set<String> names = new HashSet<>();
        MutableList<Integer> namePositions = Lists.mutable.empty();
        for (INounMapping nameMapping : textualInstance.getNameMappings()) {
            names.addAll(nameMapping.getSurfaceForms().castToCollection());
            namePositions.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }

        return "name=" + textualInstance.getName() + "occurrences= " + "NameVariants: " + names.size() + ": " + names + //
                " sentences{" + Arrays.toString(namePositions.toArray()) + "}";
    }

    @Override
    public String toString() {
        Set<String> names = new HashSet<>();
        MutableList<Integer> namePositions = Lists.mutable.empty();
        Set<String> types = new HashSet<>();
        MutableList<Integer> typePositions = Lists.mutable.empty();

        for (INounMapping nameMapping : textualInstance.getNameMappings()) {
            names.addAll(nameMapping.getSurfaceForms().castToCollection());
            namePositions.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }
        for (INounMapping typeMapping : textualInstance.getTypeMappings()) {
            types.addAll(typeMapping.getSurfaceForms().castToCollection());
            typePositions.addAll(typeMapping.getMappingSentenceNo().castToCollection());
        }
        return "InstanceMapping [ uid=" + modelInstance.getUid() + ", name=" + modelInstance.getFullName() + //
                ", as=" + String.join(", ", modelInstance.getFullType()) + ", probability=" + probability + ", FOUND: " + //
                textualInstance.getName() + " : " + getTextualInstance().getType() + ", occurrences= " + //
                "NameVariants: " + names.size() + ": " + names + " sentences{" + Arrays.toString(namePositions.toArray()) + "}" + //
                ", TypeVariants: " + types.size() + ": " + types + "sentences{" + Arrays.toString(typePositions.toArray()) + "}" + "]";
    }
}
