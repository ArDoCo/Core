/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;

/**
 * Represents a trace link between an instance of the extracted model and a recommended instance.
 *
 */
public class InstanceLinkImpl implements InstanceLink {

    private final RecommendedInstance textualInstance;
    private final ModelInstance modelInstance;
    private Confidence probability;

    private InstanceLinkImpl(RecommendedInstance textualInstance, ModelInstance modelInstance) {
        this.textualInstance = textualInstance;
        this.modelInstance = modelInstance;
        this.probability = new Confidence(AggregationFunctions.AVERAGE);
    }

    /**
     * Creates a new instance link.
     *
     * @param textualInstance the recommended instance
     * @param modelInstance   the extracted instance
     * @param probability     the probability of this link
     */
    public InstanceLinkImpl(RecommendedInstance textualInstance, ModelInstance modelInstance, Claimant claimant, double probability) {
        this(textualInstance, modelInstance);
        this.probability.addAgentConfidence(claimant, probability);
    }

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    @Override
    public double getProbability() {
        return probability.getConfidence();
    }

    /**
     * Returns the recommended instance.
     *
     * @return the textual instance
     */
    @Override
    public RecommendedInstance getTextualInstance() {
        return textualInstance;
    }

    /**
     * Returns the model instance.
     *
     * @return the extracted instance
     */
    @Override
    public ModelInstance getModelInstance() {
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
        var other = (InstanceLinkImpl) obj;
        return Objects.equals(modelInstance, other.modelInstance) && Objects.equals(textualInstance, other.textualInstance);
    }

    @Override
    public String toString() {
        Set<String> names = new HashSet<>();
        MutableList<Integer> namePositions = Lists.mutable.empty();
        Set<String> types = new HashSet<>();
        MutableList<Integer> typePositions = Lists.mutable.empty();

        for (NounMapping nameMapping : textualInstance.getNameMappings()) {
            names.addAll(nameMapping.getSurfaceForms().castToCollection());
            namePositions.addAll(nameMapping.getMappingSentenceNo().castToCollection());
        }
        for (NounMapping typeMapping : textualInstance.getTypeMappings()) {
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
