package edu.kit.ipd.consistency_analyzer.datastructures;

public interface IRelationLink {

	/**
	 * Returns the probability of the correctness of this link.
	 *
	 * @return probability of the mapping.
	 */
	public double getProbability();

	/**
	 * Sets the probability of this mapping to the given probability.
	 *
	 * @param probability the new probability
	 */
	public void setProbability(double probability);

	/**
	 * Returns the recommended relation of this link.
	 *
	 * @return the textual relation
	 */
	public IRecommendedRelation getTextualRelation();

	/**
	 * Returns the relation of the model extraction state of this link.
	 *
	 * @return the relation of the model
	 */
	public IRelation getModelRelation();

}