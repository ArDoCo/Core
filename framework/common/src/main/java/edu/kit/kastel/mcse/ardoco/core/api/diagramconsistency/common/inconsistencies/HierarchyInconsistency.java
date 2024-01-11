/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Indicates that the hierarchy is inconsistent, meaning the parent of a box is not as expected.
 * 
 * @param <B> The type of the box.
 * @param <E> The type of the entity.
 */
public class HierarchyInconsistency<B, E> extends Inconsistency<B, E> {
    private final B expectedParent;

    /**
     * Creates a new NameInconsistency.
     *
     * @param box
     *                       The box that is inconsistent.
     * @param entity
     *                       The entity that is inconsistent.
     * @param expectedParent
     *                       The expected parent of the box.
     */
    public HierarchyInconsistency(B box, E entity, B expectedParent) {
        super(box, entity);
        this.expectedParent = expectedParent;
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new HierarchyInconsistency<>(boxMapper.apply(this.getBox()), entityMapper.apply(this.getEntity()), this.getExpectedParent() == null ?
                null :
                boxMapper.apply(this.getExpectedParent()));
    }

    /**
     * The expected parent of the box.
     *
     * @return The expected parent of the box.
     */
    public B getExpectedParent() {
        return this.expectedParent;
    }

    @Override
    public B getOtherBox() {
        return this.getExpectedParent();
    }

    @Override
    public InconsistencyType getType() {
        return InconsistencyType.HIERARCHY_INCONSISTENCY;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        if (!super.equals(object))
            return false;
        HierarchyInconsistency<?, ?> that = (HierarchyInconsistency<?, ?>) object;
        return Objects.equals(this.getExpectedParent(), that.getExpectedParent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getExpectedParent());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("expectedParent", this.expectedParent).toString();
    }
}
