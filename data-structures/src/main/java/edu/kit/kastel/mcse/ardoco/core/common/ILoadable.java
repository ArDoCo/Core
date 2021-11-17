package edu.kit.kastel.mcse.ardoco.core.common;

/**
 * Defines loadable elements.
 *
 * @see Loader
 */
@FunctionalInterface
public interface ILoadable {

    /**
     * Gets the identifier of the loadable.
     *
     * @return the id
     */
    String getId();

}
