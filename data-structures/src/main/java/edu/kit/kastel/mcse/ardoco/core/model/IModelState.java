/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.model;


import edu.kit.kastel.mcse.ardoco.core.common.IState;
import java.util.Set;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * The Interface IModelState defines the information directly extracted from the models.
 */
public interface IModelState extends IState<IModelState> {
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

}
