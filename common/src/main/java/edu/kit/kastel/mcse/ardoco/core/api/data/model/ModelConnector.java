/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.model;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * The Interface IModelConnector defines the connection to a computational model.
 */
public interface ModelConnector {

    String getModelId();

    Metamodel getMetamodel();

    /**
     * Gets the instances of the model.
     *
     * @return the instances
     */
    ImmutableList<ModelInstance> getInstances();

}
