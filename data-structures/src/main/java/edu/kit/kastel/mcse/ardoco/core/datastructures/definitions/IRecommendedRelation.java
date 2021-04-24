package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface IRecommendedRelation {

    IRecommendedRelation createCopy();

    /**
     * Returns the nodes representing the relation.
     *
     * @return nodes representing the relation
     */
    List<IWord> getNodes();

    /**
     * Adds more occurrences as a list of nodes.
     *
     * @param occs list of nodes; every node represents the relation
     */
    void addOccurrences(List<IWord> occs);

    /**
     * updates the probability of the recommended relation.
     *
     * @param probability2 the probability to update with
     */
    void updateProbability(double probability2);

    /**
     * Returns the name of this relation.
     *
     * @return the name of the relation
     */
    String getName();

    /**
     * Returns the probability of this relation.
     *
     * @return the probability of the relation
     */
    double getProbability();

    /**
     * Sets the probability to the given probability.
     *
     * @param probability the new probability
     */
    void setProbability(double probability);

    /**
     * Returns the end points as instances of this relation.
     *
     * @return the involved recommended instances of the relation as list
     */
    List<IRecommendedInstance> getRelationInstances();

}
