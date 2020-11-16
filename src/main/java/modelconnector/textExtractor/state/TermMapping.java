package modelconnector.textExtractor.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the concatenation of words to one term. A term has at
 * least two words stored as noun mappings.
 *
 * @author Sophie
 *
 */
public class TermMapping {

	private String reference;
	private List<NounMapping> mappings;
	private double probability;
	private MappingKind kind;

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
	public TermMapping(String reference, NounMapping mapping1, NounMapping mapping2, MappingKind kind, double probability) {
		this.reference = reference;
		this.mappings = new ArrayList<>();
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
	 * @param mapping1      the first word of the term
	 * @param mapping2      the second word of the term
	 * @param otherMappings the other mappings in order of their occurrence.
	 * @param kind          the kind of the term
	 * @param probability   the probability that these words build a term of that
	 *                      kind
	 */
	public TermMapping(String reference, NounMapping mapping1, NounMapping mapping2, List<NounMapping> otherMappings, MappingKind kind, double probability) {
		this.reference = reference;
		this.mappings = new ArrayList<>();
		mappings.add(mapping1);
		mappings.add(mapping2);
		mappings.addAll(otherMappings);
		this.probability = probability;
		this.kind = kind;

	}

	/**
	 * Returns the mappings, of the term.
	 *
	 * @return a list of mappings of the term.
	 */
	public List<NounMapping> getMappings() {
		return mappings;
	}

	/**
	 * Returns the probability that this mapping is a term of the given kind (multi
	 * type).
	 *
	 * @return the probability of this mapping as double.
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Returns the reference of this term.
	 *
	 * @return the reference of the term.
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Returns the type of this term.
	 *
	 * @return the type of this term.
	 */
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
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
	public void updateProbability(double probability2) {
		if (probability == 1.0) {
			return;
		} else if (probability2 == 1.0) {
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
	public void hardSetProbability(double probability) {
		this.probability = probability;
	}

}
