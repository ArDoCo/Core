/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.model;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;

/**
 * The Interface IModelInstance defines instances from models.
 */
public interface IModelInstance extends ICopyable<IModelInstance> {

    /**
     * Returns the full name of the instance.
     *
     * @return the original name of the instance
     */
    String getFullName();

    /**
     * Returns the full type of the instance.
     *
     * @return the original type of the instance
     */
    String getFullType();

    /**
     * Returns all name parts of the instance.
     *
     * @return all name parts of the instance as list
     */
    ImmutableList<String> getNameParts();

    /**
     * Returns all type parts of the instance.
     *
     * @return all type parts of the instance as list
     */
    ImmutableList<String> getTypeParts();

    /**
     * Returns the unique identifier of the instance.
     *
     * @return the unique identifier of the instance
     */
    String getUid();

}
