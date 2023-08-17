/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMappingChangeListener;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * This class represents recommended instances. These instances should be contained by the model. The likelihood is
 * measured by the probability. Every recommended instance has a unique name.
 */
public class RecommendedInstanceImpl extends RecommendedInstance implements Claimant, NounMappingChangeListener {

    private static final AggregationFunctions GLOBAL_AGGREGATOR = AggregationFunctions.AVERAGE;
    /**
     * Meaning (Weights): <br/>
     * 0,-1,1 => Balanced <br/>
     * 2 => InternalConfidence: 2 / mappingConfidence: 1<br/>
     * -2 => InternalConfidence: 1 / mappingConfidence: 2<br/>
     * ...
     */
    private int weightInternalConfidence = 0;

    private String type;
    private String name;
    private Confidence internalConfidence;
    private final MutableSortedSet<NounMapping> typeMappings;
    private final MutableSortedSet<NounMapping> nameMappings;

    private RecommendedInstanceImpl(String name, String type) {
        super(name, UUID.randomUUID().toString());
        this.type = type;
        this.name = name;
        this.internalConfidence = new Confidence(AggregationFunctions.AVERAGE);
        nameMappings = SortedSets.mutable.empty();
        typeMappings = SortedSets.mutable.empty();
    }

    @Override
    public void onDelete(NounMapping deletedNounMapping, NounMapping replacement) {
        if (replacement == null)
            throw new IllegalArgumentException("Replacement should not be null!");

        if (this.nameMappings.remove(deletedNounMapping)) {
            this.nameMappings.add(replacement);
            replacement.registerChangeListener(this);
        } else if (this.typeMappings.remove(deletedNounMapping)) {
            this.typeMappings.add(replacement);
            replacement.registerChangeListener(this);
        } else {
            throw new IllegalArgumentException("Try to delete an unknown noun mapping: " + deletedNounMapping);
        }
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
    public RecommendedInstanceImpl(String name, String type, Claimant claimant, double probability, ImmutableList<NounMapping> nameNodes,
            ImmutableList<NounMapping> typeNodes) {
        this(name, type);
        this.internalConfidence.addAgentConfidence(claimant, probability);

        nameMappings.addAll(nameNodes.castToCollection());
        typeMappings.addAll(typeNodes.castToCollection());

        nameMappings.forEach(nm -> nm.registerChangeListener(this));
        typeMappings.forEach(nm -> nm.registerChangeListener(this));
    }

    private static double calculateMappingProbability(ImmutableList<NounMapping> nameMappings, ImmutableList<NounMapping> typeMappings) {
        var highestNameProbability = nameMappings.collectDouble(nm -> nm.getProbabilityForKind(MappingKind.NAME)).maxIfEmpty(0);
        var highestTypeProbability = typeMappings.collectDouble(nm -> nm.getProbabilityForKind(MappingKind.TYPE)).maxIfEmpty(0);

        return CommonUtilities.rootMeanSquare(highestNameProbability, highestTypeProbability);
    }

    /**
     * Returns the involved name mappings.
     *
     * @return the name mappings of this recommended instance
     */
    @Override
    public ImmutableList<NounMapping> getNameMappings() {
        return Lists.immutable.withAll(nameMappings);
    }

    /**
     * Returns the involved type mappings.
     *
     * @return the type mappings of this recommended instance
     */
    @Override
    public ImmutableList<NounMapping> getTypeMappings() {
        return Lists.immutable.withAll(typeMappings);
    }

    /**
     * Returns the probability being an instance of the model.
     *
     * @return the probability to be found in the model
     */
    @Override
    public double getProbability() {
        var mappingProbability = calculateMappingProbability(getNameMappings(), getTypeMappings());
        var ownProbability = internalConfidence.getConfidence();
        List<Double> probabilities = new ArrayList<>();
        probabilities.add(mappingProbability);
        probabilities.add(ownProbability);

        if (Math.abs(weightInternalConfidence) > 1) {
            var element = weightInternalConfidence > 0 ? ownProbability : mappingProbability;
            for (int i = 0; i < Math.abs(weightInternalConfidence) - 1; i++) {
                probabilities.add(element);
            }
        }

        return GLOBAL_AGGREGATOR.applyAsDouble(probabilities);
    }

    /**
     * Adds a name and type mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     * @param typeMapping the type mapping to add
     */
    @Override
    public void addMappings(NounMapping nameMapping, NounMapping typeMapping) {
        addName(nameMapping);
        addType(typeMapping);
        nameMapping.registerChangeListener(this);
        typeMapping.registerChangeListener(this);
    }

    /**
     * Adds name and type mappings to this recommended instance.
     *
     * @param nameMapping the name mappings to add
     * @param typeMapping the type mappings to add
     */
    @Override
    public void addMappings(ImmutableList<NounMapping> nameMapping, ImmutableList<NounMapping> typeMapping) {
        nameMapping.forEach(this::addName);
        typeMapping.forEach(this::addType);

        nameMapping.forEach(nm -> nm.registerChangeListener(this));
        typeMapping.forEach(nm -> nm.registerChangeListener(this));
    }

    /**
     * Adds a name mapping to this recommended instance.
     *
     * @param nameMapping the name mapping to add
     */
    @Override
    public void addName(NounMapping nameMapping) {
        nameMappings.add(nameMapping);
        nameMapping.registerChangeListener(this);
    }

    /**
     * Adds a type mapping to this recommended instance.
     *
     * @param typeMapping the type mapping to add
     */
    @Override
    public void addType(NounMapping typeMapping) {
        typeMappings.add(typeMapping);
        typeMapping.registerChangeListener(this);
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
    public void addProbability(Claimant claimant, double probability) {
        this.internalConfidence.addAgentConfidence(claimant, probability);
    }

    @Override
    public ImmutableSortedSet<Integer> getSentenceNumbers() {
        MutableSortedSet<Integer> sentenceNos = getNameMappings().flatCollect(nm -> nm.getWords().collect(Word::getSentenceNo)).toSortedSet();
        return sentenceNos.toImmutableSortedSet();
    }

    @Override
    public String toString() {
        var separator = "\n\t\t\t\t\t";
        MutableList<String> typeNodeVals = Lists.mutable.empty();
        for (NounMapping typeMapping : typeMappings) {
            typeNodeVals.add(typeMapping.toString());
        }

        MutableList<String> nameNodeVals = Lists.mutable.empty();
        for (NounMapping nameMapping : nameMappings) {
            nameNodeVals.add(nameMapping.toString());
        }
        return "RecommendationInstance [" + " name=" + name + ", type=" + type + ", probability=" + getProbability() + //
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
        if (!(obj instanceof RecommendedInstanceImpl other)) {
            return false;
        }
        return Objects.equals(name, other.name) && Objects.equals(type, other.type);
    }

    @Override
    public ImmutableSortedSet<Claimant> getClaimants() {
        return SortedSets.immutable.withAll(this.internalConfidence.getClaimants());
    }

}
