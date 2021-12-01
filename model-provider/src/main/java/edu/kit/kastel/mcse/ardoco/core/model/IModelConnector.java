/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.model;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * The Interface IModelConnector defines the connection to a computational model.
 */
public interface IModelConnector {

    /**
     * Gets the instances of the model.
     *
     * @return the instances
     */
    ImmutableList<IModelInstance> getInstances();

    /**
     * Gets the relations from the model.
     *
     * @return the relations
     */
    ImmutableList<IModelRelation> getRelations();

}
