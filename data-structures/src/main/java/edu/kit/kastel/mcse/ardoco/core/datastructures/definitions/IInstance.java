package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

/**
 * The Interface IInstance defines instances from models.
 */
public interface IInstance {
    /**
     * Create a deep copy of the instance.
     *
     * @return a copy of the instance
     */
    IInstance createCopy();

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
    List<String> getNames();

    /**
     * Returns all type parts of the instance.
     *
     * @return all type parts of the instance as list
     */
    List<String> getTypes();

    /**
     * Returns the unique identifier of the instance.
     *
     * @return the unique identifier of the instance
     */
    String getUid();

}
