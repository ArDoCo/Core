/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Indicates that a box is missing a line to another box.
 * 
 * @param <B> The type of the box.
 * @param <E> The type of the entity.
 */
public class MissingLineInconsistency<B, E> extends Inconsistency<B, E> {
    private final B expectedLineTarget;

    /**
     * Creates a new MissingLineInconsistency.
     *
     * @param box
     *                           The box that is the source of the missing line.
     * @param expectedLineTarget
     *                           The box that is the target of the missing line.
     */
    public MissingLineInconsistency(B box, B expectedLineTarget) {
        super(box, null);
        this.expectedLineTarget = expectedLineTarget;
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new MissingLineInconsistency<>(boxMapper.apply(this.getBox()), boxMapper.apply(this.getExpectedLineTarget()));
    }

    /**
     * The box that is the target of the missing line.
     *
     * @return The box that is the target of the missing line.
     */
    public B getExpectedLineTarget() {
        return this.expectedLineTarget;
    }

    @Override
    public B getOtherBox() {
        return this.getExpectedLineTarget();
    }

    @Override
    public InconsistencyType getType() {
        return InconsistencyType.MISSING_LINE;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        if (!super.equals(object))
            return false;
        MissingLineInconsistency<?, ?> that = (MissingLineInconsistency<?, ?>) object;
        return Objects.equals(this.getExpectedLineTarget(), that.getExpectedLineTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getExpectedLineTarget());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("expectedLineTarget", this.expectedLineTarget).toString();
    }
}
