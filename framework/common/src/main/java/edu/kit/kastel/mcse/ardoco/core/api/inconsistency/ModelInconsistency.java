/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.inconsistency;

/**
 * This interface extends the interface {@link Inconsistency} by stating that the inconsistency stems from a concrete model instance, i.e., a component.
 * This way, we can output information about the inconsistent model instance.
 */
public interface ModelInconsistency extends Inconsistency {

    /**
     * Return the name of the inconsistent model instance.
     * 
     * @return the name of the inconsistent model instance.
     */
    String getEntityName();

    /**
     * Return the type of the inconsistent model instance.
     * 
     * @return the type of the inconsistent model instance.
     */
    String getEntityType();

    /**
     * Return the UID of the inconsistent model instance.
     * 
     * @return the UID of the inconsistent model instance.
     */
    String getInstanceId();
}
