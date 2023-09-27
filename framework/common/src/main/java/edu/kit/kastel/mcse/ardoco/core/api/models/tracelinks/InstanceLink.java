/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * An InstanceLink defines a link between an {@link RecommendedInstance} and an {@link Entity}.
 */
public class InstanceLink extends EndpointTuple {

    private final RecommendedInstance textualInstance;
    private final Entity entity;
    private Confidence confidence;

    /**
     * Create a new instance link
     *
     * @param textualInstance the recommended instance
     * @param entity   the model instance
     */
    public InstanceLink(RecommendedInstance textualInstance, Entity entity) {
        super(textualInstance, entity);
        this.textualInstance = textualInstance;
        this.entity = entity;
        this.confidence = new Confidence(AggregationFunctions.AVERAGE);
    }

    /**
     * Creates a new instance link.
     *
     * @param textualInstance the recommended instance
     * @param entity   the model instance
     * @param claimant        the claimant
     * @param probability     the probability of this link
     */
    public InstanceLink(RecommendedInstance textualInstance, Entity entity, Claimant claimant, double probability) {
        this(textualInstance, entity);
        this.confidence.addAgentConfidence(claimant, probability);
    }

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return the probability of this link
     */
    public final double getConfidence() {
        return confidence.getConfidence();
    }

    /**
     * Returns the recommended instance.
     *
     * @return the textual instance
     */
    public final RecommendedInstance getTextualInstance() {
        return textualInstance;
    }

    /**
     * Returns the model instance.
     *
     * @return the extracted instance
     */
    public final Entity getEntity() {
        return entity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, textualInstance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InstanceLink other)) {
            return false;
        }
        return Objects.equals(getEntity(), other.getEntity()) && Objects.equals(getTextualInstance(), other.getTextualInstance());
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
        return "InstanceMapping [ uid=" + entity.getId() + ", name=" + entity.getName() + //
                ", as=" + String.join(", ", entity.getClass().getName()) + ", probability=" + getConfidence() + ", FOUND: " + //
                textualInstance.getName() + " : " + textualInstance.getType() + ", occurrences= " + //
                "NameVariants: " + names.size() + ": " + names + " sentences{" + Arrays.toString(namePositions.toArray()) + "}" + //
                ", TypeVariants: " + types.size() + ": " + types + "sentences{" + Arrays.toString(typePositions.toArray()) + "}" + "]";
    }

}
