/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.model;

import java.util.Set;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.informalin.framework.configuration.IConfigurable;

/**
 * The Interface IModelState defines the information directly extracted from the models.
 */
public interface IModelState extends ICopyable<IModelState>, IConfigurable {
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
    ImmutableList<IModelInstance> getInstancesOfType(String type);

    /**
     * Returns the relations of a specific type.
     *
     * @param type the type to search for
     * @return all relations that are from that type
     */
    ImmutableList<IModelRelation> getRelationsOfType(String type);

    /**
     * Returns all types that are contained by instances of this state.
     *
     * @return all instance types of this state
     */
    Set<String> getInstanceTypes();

    /**
     * Returns all types that are contained by relations of this state.
     *
     * @return all relation types of this state
     */
    Set<String> getRelationTypes();

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
    ImmutableList<IModelInstance> getInstances();

    /**
     * Returns all relations that are contained by this state.
     *
     * @return all relations of this state
     */
    ImmutableList<IModelRelation> getRelations();

    void addAllOf(IModelState other);

}
