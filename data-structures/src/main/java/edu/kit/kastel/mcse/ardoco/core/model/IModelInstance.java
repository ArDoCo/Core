package edu.kit.kastel.mcse.ardoco.core.model;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;

/**
 * The Interface IModelInstance defines instances from models.
 */
public interface IModelInstance extends ICopyable<IModelInstance> {

    /**
     * Returns the longest name of the instance.
     *
     * @return the original name of the instance
     */
    String getLongestName();

    /**
     * Returns the longest type of the instance.
     *
     * @return the original type of the instance
     */
    String getLongestType();

    /**
     * Returns all name parts of the instance.
     *
     * @return all name parts of the instance as list
     */
    ImmutableList<String> getNames();

    /**
     * Returns all type parts of the instance.
     *
     * @return all type parts of the instance as list
     */
    ImmutableList<String> getTypes();

    /**
     * Returns the unique identifier of the instance.
     *
     * @return the unique identifier of the instance
     */
    String getUid();

}
