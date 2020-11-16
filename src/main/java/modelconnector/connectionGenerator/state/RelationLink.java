package modelconnector.connectionGenerator.state;

import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendedRelation;

/**
 * Represents a trace link between a relation of the extracted model and a
 * recommended relation.
 *
 * @author Sophie
 *
 */
public class RelationLink {

	private RecommendedRelation textualRelation;
	private Relation modelRelation;
	private double probability;

	/**
	 * Creates a new relation mapping
	 *
	 * @param textRelation  recommended relation
	 * @param modelRelation relation from the model extraction state
	 * @param probability   probability for similarity
	 */
	public RelationLink(RecommendedRelation textRelation, Relation modelRelation, double probability) {
		this.textualRelation = textRelation;
		this.modelRelation = modelRelation;
		this.probability = probability;
	}

	/**
	 * Returns the probability of the correctness of this link.
	 *
	 * @return probability of the mapping.
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Sets the probability of this mapping to the given probability.
	 *
	 * @param probability the new probability
	 */
	public void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * Returns the recommended relation of this link.
	 *
	 * @return the textual relation
	 */
	public RecommendedRelation getTextualRelation() {
		return textualRelation;
	}

	/**
	 * Returns the relation of the model extraction state of this link.
	 *
	 * @return the relation of the model
	 */
	public Relation getModelRelation() {
		return modelRelation;
	}

	@Override
	public String toString() {

		return "RelationLink: " + this.getModelRelation().toString() + " by " + this.getTextualRelation().toString() + "probability: " + probability;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modelRelation == null) ? 0 : modelRelation.hashCode());
		result = prime * result + ((textualRelation == null) ? 0 : textualRelation.hashCode());
		return result;
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
		RelationLink other = (RelationLink) obj;
		if (modelRelation == null) {
			if (other.modelRelation != null) {
				return false;
			}
		} else if (!modelRelation.equals(other.modelRelation)) {
			return false;
		}
		if (textualRelation == null) {
			if (other.textualRelation != null) {
				return false;
			}
		} else if (!textualRelation.equals(other.textualRelation)) {
			return false;
		}
		return true;
	}

}
