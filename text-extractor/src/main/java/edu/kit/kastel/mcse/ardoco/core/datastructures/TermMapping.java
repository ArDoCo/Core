package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITermMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

/**
 * This class represents the concatenation of words to one term. A term has at
 * least two words stored as noun mappings.
 *
 * @author Sophie
 *
 */
public class TermMapping implements ITermMapping {

	private String reference;
	private List<INounMapping> mappings;
	private double probability;
	private MappingKind kind;

	@Override
	public ITermMapping createCopy() {

		return new TermMapping(reference, mappings.stream().map(INounMapping::createCopy).collect(Collectors.toList()), probability, kind);

	}

	public TermMapping(String reference, List<INounMapping> mappings, double probability, MappingKind kind) {
		this.reference = reference;
		this.kind = kind;
		this.probability = probability;
		this.mappings = mappings;
	}

	/**
	 * Creates a new term out of two terms (noun mappings).
	 *
	 * @param reference   the reference of the term. With its reference it is
	 *                    compared.
	 * @param mapping1    the first word of the term
	 * @param mapping2    the second word of the term
	 * @param kind        the kind of the term
	 * @param probability the probability that these words build a term of that kind
	 */
	public TermMapping(String reference, INounMapping mapping1, INounMapping mapping2, MappingKind kind, double probability) {
		this.reference = reference;
		mappings = new ArrayList<>();
		mappings.add(mapping1);
		mappings.add(mapping2);
		this.probability = probability;
		this.kind = kind;

	}

	/**
	 * Creates a new term out of two terms (noun mappings).
	 *
	 * @param reference     the reference of the term. With its reference it is
	 *                      compared.
	 * @param iNounMapping  the first word of the term
	 * @param iNounMapping2 the second word of the term
	 * @param list          the other mappings in order of their occurrence.
	 * @param kind          the kind of the term
	 * @param probability   the probability that these words build a term of that
	 *                      kind
	 */
	public TermMapping(String reference, INounMapping iNounMapping, INounMapping iNounMapping2, List<INounMapping> list, MappingKind kind, double probability) {
		this.reference = reference;
		mappings = new ArrayList<>();
		mappings.add(iNounMapping);
		mappings.add(iNounMapping2);
		mappings.addAll(list);
		this.probability = probability;
		this.kind = kind;

	}

	/**
	 * Returns the mappings, of the term.
	 *
	 * @return a list of mappings of the term.
	 */
	@Override
	public List<INounMapping> getMappings() {
		return mappings;
	}

	/**
	 * Returns the probability that this mapping is a term of the given kind (multi
	 * type).
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
	 * @param probability2 the probability to update with.
	 */
	@Override
	public void updateProbability(double probability2) {
		if (probability == 1.0) {
			return;
		}

		if (probability2 == 1.0) {
			probability = 1.0;
		} else if (probability >= probability2) {
			probability += probability2 * (1 - probability);
		} else {
			probability += probability2;
			probability = probability * 0.5;
		}
	}

	/**
	 * Sets the probability of the mapping
	 *
	 * @param probability probability to set on
	 */
	@Override
	public void hardSetProbability(double probability) {
		this.probability = probability;
	}

}
