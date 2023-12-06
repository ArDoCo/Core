/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;

/**
 * A group of inconsistencies that all belong to the same box.
 *
 * @param <B>
 *            The type of the box.
 * @param <E>
 *            The type of the entity.
 */
public class Group<B, E> extends Inconsistency<B, E> {
    private final List<Inconsistency<B, E>> inconsistencies;

    /**
     * Creates a new Group.
     *
     * @param box
     *                        The box.
     * @param inconsistencies
     *                        The inconsistencies.
     */
    public Group(B box, List<Inconsistency<B, E>> inconsistencies) {
        super(box, null);
        this.inconsistencies = inconsistencies;
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new Group<>(boxMapper.apply(this.getBox()), this.inconsistencies.stream()
                .map(inconsistency -> inconsistency.map(boxMapper, entityMapper))
                .toList());
    }

    @Override
    public InconsistencyType getType() {
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        if (!super.equals(object))
            return false;
        Group<?, ?> group = (Group<?, ?>) object;
        return Objects.equals(this.inconsistencies, group.inconsistencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.inconsistencies);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("inconsistencies", this.inconsistencies).toString();
    }
}
