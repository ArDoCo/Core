package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import org.eclipse.jgit.annotations.Nullable;

/**
 * The vertex type used in the graphs that are matched.
 *
 * @param <T>
 *         The element type, which is the original element that the vertex represents, e.g. a Box or a element from the
 *         model.
 */
public final class Vertex<T> {
    @Nullable
    private final T represented;
    private String name;

    /**
     * @param represented
     *         The element that the vertex represents.
     * @param name
     *         The name of the vertex, which is used for matching.
     */
    public Vertex(@Nullable T represented, String name) {
        this.represented = represented;
        this.name = name;
    }

    /**
     * Get the element that the vertex represents.
     *
     * @return The element that the vertex represents.
     */
    @Nullable
    public T getRepresented() {
        return this.represented;
    }

    /**
     * Get the name of the vertex, which is used for matching.
     *
     * @return The name of the vertex, which is used for matching.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of the vertex.
     *
     * @param newName
     *         The new name of the vertex.
     */
    public void rename(String newName) {
        this.name = newName;
    }

    /**
     * Creates a copy of the vertex.
     *
     * @return The copy of the vertex.
     */
    public Vertex<T> copy() {
        return new Vertex<>(this.represented, this.name);
    }
}
