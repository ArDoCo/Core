/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.model;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

/**
 * The Interface IModelConnector defines the connection to a computational model.
 */
public interface IModelConnector {

    String getModelId();

    Metamodel getMetamodel();

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
