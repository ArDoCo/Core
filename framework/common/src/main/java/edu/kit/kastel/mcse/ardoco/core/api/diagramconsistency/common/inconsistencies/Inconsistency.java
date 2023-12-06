/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jgit.annotations.Nullable;

/**
 * Base class for all inconsistencies between the diagram and the given models.
 * 
 * @param <B> The type of the box.
 * @param <E> The type of the entity.
 */
public abstract class Inconsistency<B, E> {
    private final B box;
    private final E entity;

    protected Inconsistency(B box, E entity) {
        this.box = box;
        this.entity = entity;
    }

    /**
     * Map the inconsistency to the same inconsistency type with different box and entity types.
     *
     * @param boxMapper
     *                     The mapper for the box.
     * @param entityMapper
     *                     The mapper for the entity.
     * @param <R>
     *                     The type of the box (representation) of the mapped inconsistency.
     * @param <M>
     *                     The type of the entity (model) of the mapped inconsistency.
     * @return The mapped inconsistency.
     */
    public abstract <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper);

    /**
     * Get the inconsistency type.
     *
     * @return The inconsistency type.
     */
    public abstract InconsistencyType getType();

    /**
     * Get the box that is part of the inconsistency.
     *
     * @return The box, or null if there is no box.
     */
    public @Nullable B getBox() {
        return this.box;
    }

    /**
     * Get the other box that is part of the inconsistency, if there is one. This is relevant for inconsistencies
     * related to lines between boxes.
     *
     * @return The other box, or null if there is no other box.
     */
    public @Nullable B getOtherBox() {
        return null;
    }

    /**
     * Get the entity that is part of the inconsistency.
     *
     * @return The entity, or null if there is no entity.
     */
    public @Nullable E getEntity() {
        return this.entity;
    }

    /**
     * Get the name of the box that is part of the inconsistency.
     *
     * @return The name, or null if not a name inconsistency.
     */
    public @Nullable String getActualName() {
        return null;
    }

    /**
     * Get the expected name of the box that is part of the inconsistency.
     *
     * @return The expected name, or null if not a name inconsistency.
     */
    public @Nullable String getExpectedName() {
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        Inconsistency<?, ?> that = (Inconsistency<?, ?>) object;
        return Objects.equals(this.getBox(), that.getBox()) && Objects.equals(this.getEntity(), that.getEntity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getBox(), this.getEntity());
    }
}
