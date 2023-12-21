/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import java.util.Objects;
import java.util.function.Function;

/**
 * Indicates that the name of a box and the name of an entity are inconsistent.
 * 
 * @param <B> The type of the box.
 * @param <E> The type of the entity.
 */
public class NameInconsistency<B, E> extends Inconsistency<B, E> {
    private final String actualName;
    private final String expectedName;

    /**
     * Creates a new NameInconsistency.
     *
     * @param box
     *                     The box that is part of the inconsistency.
     * @param entity
     *                     The entity that is part of the inconsistency.
     * @param expectedName
     *                     The name that the entity has.
     * @param actualName
     *                     The name that the box has.
     */
    public NameInconsistency(B box, E entity, String expectedName, String actualName) {
        super(box, entity);

        if (box == null || entity == null) {
            throw new IllegalArgumentException("Both box and entity must not be null.");
        }

        this.actualName = actualName;
        this.expectedName = expectedName;
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new NameInconsistency<>(boxMapper.apply(this.getBox()), entityMapper.apply(this.getEntity()), this.getExpectedName(), this.getActualName());
    }

    @Override
    public String getActualName() {
        return this.actualName;
    }

    @Override
    public String getExpectedName() {
        return this.expectedName;
    }

    @Override
    public InconsistencyType getType() {
        return InconsistencyType.NAME_INCONSISTENCY;
    }

    @Override
    public String toString() {
        return String.format("NameInconsistency[box=%s, entity=%s, actualName=%s, expectedName=%s]", this.getBox(), this.getEntity(), this.actualName,
                this.expectedName);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        if (!super.equals(object))
            return false;
        NameInconsistency<?, ?> that = (NameInconsistency<?, ?>) object;
        return Objects.equals(this.getActualName(), that.getActualName()) && Objects.equals(this.getExpectedName(), that.getExpectedName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getActualName(), this.getExpectedName());
    }
}
