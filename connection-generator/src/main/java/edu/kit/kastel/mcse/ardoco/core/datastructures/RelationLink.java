package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelationLink;

/**
 * Represents a trace link between a relation of the extracted model and a
 * recommended relation.
 *
 * @author Sophie
 *
 */
public class RelationLink implements IRelationLink {

	private IRecommendedRelation textualRelation;
	private IRelation modelRelation;
	private double probability;

	@Override
	public IRelationLink createCopy() {
		return new RelationLink(textualRelation.createCopy(), modelRelation.createCopy(), probability);
	}

	/**
	 * Creates a new relation mapping
	 *
	 * @param textRelation  recommended relation
	 * @param modelRelation relation from the model extraction state
	 * @param probability   probability for similarity
	 */
	public RelationLink(IRecommendedRelation textRelation, IRelation modelRelation, double probability) {
		textualRelation = textRelation;
		this.modelRelation = modelRelation;
		this.probability = probability;
	}

	@Override
	public int hashCode() {
		return Objects.hash(modelRelation, textualRelation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		RelationLink other = (RelationLink) obj;
		return Objects.equals(modelRelation, other.modelRelation) && Objects.equals(textualRelation, other.textualRelation);
	}

	/**
	 * Returns the probability of the correctness of this link.
	 *
	 * @return probability of the mapping.
	 */
	@Override
	public double getProbability() {
		return probability;
	}

	/**
	 * Sets the probability of this mapping to the given probability.
	 *
	 * @param probability the new probability
	 */
	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * Returns the recommended relation of this link.
	 *
	 * @return the textual relation
	 */
	@Override
	public IRecommendedRelation getTextualRelation() {
		return textualRelation;
	}

	/**
	 * Returns the relation of the model extraction state of this link.
	 *
	 * @return the relation of the model
	 */
	@Override
	public IRelation getModelRelation() {
		return modelRelation;
	}

	@Override
	public String toString() {

		return "RelationLink: " + getModelRelation().toString() + " by " + getTextualRelation().toString() + "probability: " + probability;
	}

}
