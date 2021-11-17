package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedRelation;

/**
 * The Interface IRelationLink defines a link between a model relation and a texual element.
 */
public interface IRelationLink extends ICopyable<IRelationLink> {

    /**
     * Returns the probability of the correctness of this link.
     *
     * @return probability of the mapping.
     */
    double getProbability();

    /**
     * Sets the probability of this mapping to the given probability.
     *
     * @param probability the new probability
     */
    void setProbability(double probability);

    /**
     * Returns the recommended relation of this link.
     *
     * @return the textual relation
     */
    IRecommendedRelation getTextualRelation();

    /**
     * Returns the relation of the model extraction state of this link.
     *
     * @return the relation of the model
     */
    IModelRelation getModelRelation();

}
