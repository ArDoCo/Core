/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies;

import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Indicates that an expected box is not present in the diagram.
 * 
 * @param <B> The type of the box.
 * @param <E> The type of the entity.
 */
public class MissingBoxInconsistency<B, E> extends Inconsistency<B, E> {
    /**
     * Creates a new MissingBoxInconsistency.
     *
     * @param entity
     *               The entity that is not represented in the diagram.
     */
    public MissingBoxInconsistency(E entity) {
        super(null, entity);
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new MissingBoxInconsistency<>(entityMapper.apply(this.getEntity()));
    }

    @Override
    public InconsistencyType getType() {
        return InconsistencyType.MISSING_BOX;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
