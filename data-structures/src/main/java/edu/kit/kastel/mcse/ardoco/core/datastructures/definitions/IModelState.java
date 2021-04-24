package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IState;

public interface IModelState extends IState {

    IModelState createCopy();

    /**
     * Returns the instances of a specific type.
     *
     * @param type the type to search for
     * @return all instances that are from that type
     */
    List<IInstance> getInstancesOfType(String type);

    /**
     * Returns the relations of a specific type.
     *
     * @param type the type to search for
     * @return all relations that are from that type
     */
    List<IRelation> getRelationsOfType(String type);

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
    List<IInstance> getInstances();

    /**
     * Returns all relations that are contained by this state.
     *
     * @return all relations of this state
     */
    List<IRelation> getRelations();

    @Override
    String toString();

}
