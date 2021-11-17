package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

/**
 * This class represents the concatenation of words to one term. A term has at least two words stored as noun mappings.
 *
 * @author Sophie
 *
 */
public class TermMapping implements ITermMapping {

    private String reference;
    private MutableList<INounMapping> mappings;
    private double probability;
    private MappingKind kind;

    @Override
    public ITermMapping createCopy() {

        return new TermMapping(reference, mappings.collect(INounMapping::createCopy).toImmutable(), probability, kind);

    }

    /**
     * Instantiates a new term mapping.
     *
     * @param reference   the reference
     * @param mappings    the mappings
     * @param probability the probability
     * @param kind        the kind
     */
    public TermMapping(String reference, ImmutableList<INounMapping> mappings, double probability, MappingKind kind) {
        this.reference = reference;
        this.kind = kind;
        this.probability = probability;
        this.mappings = mappings.toList();
    }

    /**
     * Creates a new term out of two terms (noun mappings).
     *
     * @param reference     the reference of the term. With its reference it is compared.
     * @param iNounMapping  the first word of the term
     * @param iNounMapping2 the second word of the term
     * @param list          the other mappings in order of their occurrence.
     * @param kind          the kind of the term
     * @param probability   the probability that these words build a term of that kind
     */
    public TermMapping(String reference, INounMapping iNounMapping, INounMapping iNounMapping2, ImmutableList<INounMapping> list, MappingKind kind,
            double probability) {
        this.reference = reference;
        mappings = Lists.mutable.empty();
        mappings.add(iNounMapping);
        mappings.add(iNounMapping2);
        mappings.addAll(list.castToCollection());
        this.probability = probability;
        this.kind = kind;

    }

    /**
     * Returns the mappings, of the term.
     *
     * @return a list of mappings of the term.
     */
    @Override
    public ImmutableList<INounMapping> getMappings() {
        return mappings.toImmutable();
    }

    /**
     * Returns the probability that this mapping is a term of the given kind (multi type).
     *
     * @return the probability of this mapping as double.
     */
    @Override
    public double getProbability() {
        return probability;
    }

    /**
     * Returns the reference of this term.
     *
     * @return the reference of the term.
     */
    @Override
    public String getReference() {
        return reference;
    }

    /**
     * Returns the type of this term.
     *
     * @return the type of this term.
     */
    @Override
    public MappingKind getKind() {
        return kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, mappings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TermMapping other = (TermMapping) obj;
        return kind == other.kind && Objects.equals(mappings, other.mappings);
    }

    /**
     * Updates the probability
     *
     * @param newProbability the probability to update with.
     */
    @Override
    public void updateProbability(double newProbability) {
        probability = CommonUtilities.calcNewProbabilityValue(probability, newProbability);
    }

}
