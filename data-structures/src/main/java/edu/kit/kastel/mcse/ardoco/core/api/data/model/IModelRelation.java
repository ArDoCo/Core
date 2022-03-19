/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.model;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;

/**
 * The Interface IModelRelation defines a relation extracted from the model.
 */
public interface IModelRelation extends ICopyable<IModelRelation> {

    /**
     * Adds more end points to the relation. Checks if the instance is already contained.
     *
     * @param others list of other end points of this relation
     */
    void addOtherInstances(ImmutableList<IModelInstance> others);

    /**
     * Returns the end points of this relation as instances.
     *
     * @return list of connected instances by this relation
     */
    ImmutableList<IModelInstance> getInstances();

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
