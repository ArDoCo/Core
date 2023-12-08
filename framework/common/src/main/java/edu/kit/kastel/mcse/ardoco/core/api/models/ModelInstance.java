/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * The Interface IModelInstance defines instances from models.
 */
public abstract sealed class ModelInstance extends Entity permits ModelInstanceImpl {

    protected ModelInstance(String name, String id) {
        super(name, id);
    }

    /**
     * Returns the full name of the instance.
     *
     * @return the original name of the instance
     */
    public abstract String getFullName();

    /**
     * Returns the full type of the instance.
     *
     * @return the original type of the instance
     */
    public abstract String getFullType();

    /**
     * Returns all name parts of the instance.
     *
     * @return all name parts of the instance as list
     */
    public abstract ImmutableList<String> getNameParts();

    /**
     * Returns all type parts of the instance.
     *
     * @return all type parts of the instance as list
     */
    public abstract ImmutableList<String> getTypeParts();

    /**
     * Returns the unique identifier of the instance.
     *
     * @return the unique identifier of the instance
     */
    public abstract String getUid();

}
