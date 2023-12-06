/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a box has an unexpected line.
 * 
 * @param <B> The type of the box.
 * @param <E> The type of the entity.
 */
public class UnexpectedLineInconsistency<B, E> extends Inconsistency<B, E> {
    private final B unexpectedLineTarget;

    /**
     * Creates a new UnexpectedLineInconsistency.
     *
     * @param box
     *                             The box that is the source of the unexpected line.
     * @param unexpectedLineTarget
     *                             The box that is the target of the unexpected line.
     */
    public UnexpectedLineInconsistency(B box, B unexpectedLineTarget) {
        super(box, null);
        this.unexpectedLineTarget = Objects.requireNonNull(unexpectedLineTarget);
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new UnexpectedLineInconsistency<>(boxMapper.apply(this.getBox()), boxMapper.apply(this.getUnexpectedLineTarget()));
    }

    /**
     * The box that is the target of the missing line.
     *
     * @return The box that is the target of the missing line.
     */
    public B getUnexpectedLineTarget() {
        return this.unexpectedLineTarget;
    }

    @Nullable
    @Override
    public B getOtherBox() {
        return this.getUnexpectedLineTarget();
    }

    @Override
    public InconsistencyType getType() {
        return InconsistencyType.UNEXPECTED_LINE;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        if (!super.equals(object))
            return false;
        UnexpectedLineInconsistency<?, ?> that = (UnexpectedLineInconsistency<?, ?>) object;
        return Objects.equals(this.getUnexpectedLineTarget(), that.getUnexpectedLineTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getUnexpectedLineTarget());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("unexpectedLineTarget", this.unexpectedLineTarget).toString();
    }
}
