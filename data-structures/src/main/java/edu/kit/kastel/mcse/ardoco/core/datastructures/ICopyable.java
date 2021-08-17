package edu.kit.kastel.mcse.ardoco.core.datastructures;

/**
 * The Interface ICopyable defines types that are copyable.
 *
 * @param <C> the copyable itself
 */
@FunctionalInterface
public interface ICopyable<C extends ICopyable<C>> {

    /**
     * Creates the copy of this object.
     *
     * @return the copy
     */
    C createCopy();
}
