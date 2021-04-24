package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface IInstance {

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
