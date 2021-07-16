package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ICopyable;

/**
 * The Interface IModelRelation defines a relation extracted from the model.
 */
public interface IModelRelation extends ICopyable<IModelRelation> {

    /**
     * Adds more end points to the relation. Checks if the instance is already contained.
     *
     * @param others list of other end points of this relation
     */
    void addOtherInstances(List<? extends IModelInstance> others);

    /**
     * Returns the end points of this relation as instances.
     *
     * @return list of connected instances by this relation
     */
    List<? extends IModelInstance> getInstances();

    /**
     * Returns the determiner of the relation.
     *
     * @return the type of relation
     */
    String getType();

    /**
     * Returns the unique identifier of this relation.
     *
     * @return the uid of this relation
     */
    String getUid();

}
