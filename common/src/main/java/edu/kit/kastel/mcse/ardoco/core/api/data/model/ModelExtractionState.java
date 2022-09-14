/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.model;

import java.util.Set;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.informalin.framework.configuration.IConfigurable;

/**
 * The Interface IModelState defines the information directly extracted from the models.
 */
public interface ModelExtractionState extends IConfigurable {
    /**
     * Returns the unique id of the model
     *
     * @return modelId
     */
    String getModelId();

    /**
     * Returns the metalevel or type of the current model
     *
     * @return type of model
     */
    Metamodel getMetamodel();

    /**
     * Returns the instances of a specific type.
     *
     * @param type the type to search for
     * @return all instances that are from that type
     */
    ImmutableList<ModelInstance> getInstancesOfType(String type);

    /**
     * Returns all types that are contained by instances of this state.
     *
     * @return all instance types of this state
     */
    ImmutableSet<String> getInstanceTypes();

    /**
     * Returns all names that are contained by this state.
     *
     * @return all names of this state
     */
    Set<String> getNames();

    /**
     * Returns all instances that are contained by this state.
     *
     * @return all instances of this state
     */
    ImmutableList<ModelInstance> getInstances();

    void addAllOf(ModelExtractionState other);

}
