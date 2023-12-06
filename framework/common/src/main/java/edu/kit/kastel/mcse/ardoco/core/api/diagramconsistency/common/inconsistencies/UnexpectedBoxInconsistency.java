/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Indicates that a box is unexpected in the diagram.
 * 
 * @param <B> The type of the box.
 * @param <E> The type of the entity.
 */
public class UnexpectedBoxInconsistency<B, E> extends Inconsistency<B, E> {
    /**
     * Creates a new UnexpectedBoxInconsistency.
     *
     * @param box
     *            The box that is unexpected.
     */
    public UnexpectedBoxInconsistency(B box) {
        super(box, null);
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new UnexpectedBoxInconsistency<>(boxMapper.apply(this.getBox()));
    }

    @Override
    public InconsistencyType getType() {
        return InconsistencyType.UNEXPECTED_BOX;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
