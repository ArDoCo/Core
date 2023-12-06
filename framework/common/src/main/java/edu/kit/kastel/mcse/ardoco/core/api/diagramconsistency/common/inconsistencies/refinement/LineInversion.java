/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Indicates that the direction of a line is inverted.
 *
 * @param <B>
 *            The type of the box.
 * @param <E>
 *            The type of the entity.
 */
@Deterministic
public class LineInversion<B, E> extends Inconsistency<B, E> {
    private final B expectedTarget;

    protected LineInversion(B expectedSource, B expectedTarget) {
        super(expectedSource, null);
        this.expectedTarget = expectedTarget;
    }

    /**
     * Discovers inconsistencies of this type in a set of basic inconsistencies.
     *
     * @param inconsistencies
     *                        The list of inconsistencies, will be modified.
     * @param <B>
     *                        The type used to represent boxes.
     * @param <E>
     *                        The type used to represent entities.
     * @return The list of inconsistencies, modified to include the newly discovered ones and without the ones that were
     *         used to discover new ones.
     */
    public static <B, E> List<Inconsistency<B, E>> discover(List<Inconsistency<B, E>> inconsistencies) {
        Set<Inconsistency<B, E>> newInconsistencies = new LinkedHashSet<>(inconsistencies);

        Map<B, Set<Inconsistency<B, E>>> boxesWithUnexpectedLine = getAllBoxesWithUnexpectedLines(inconsistencies);

        for (var inconsistency : inconsistencies) {
            if (inconsistency.getType() == InconsistencyType.MISSING_LINE) {
                var unexpectedLineInconsistencies = boxesWithUnexpectedLine.get(inconsistency.getOtherBox());
                if (unexpectedLineInconsistencies != null) {
                    for (var unexpectedLineInconsistency : unexpectedLineInconsistencies) {
                        if (Objects.equals(unexpectedLineInconsistency.getOtherBox(), inconsistency.getBox())) {
                            newInconsistencies.remove(unexpectedLineInconsistency);
                            newInconsistencies.remove(inconsistency);
                            newInconsistencies.add(new LineInversion<>(inconsistency.getBox(), unexpectedLineInconsistency.getBox()));
                        }
                    }
                }
            }
        }

        return new ArrayList<>(newInconsistencies);
    }

    private static <B, E> Map<B, Set<Inconsistency<B, E>>> getAllBoxesWithUnexpectedLines(List<Inconsistency<B, E>> inconsistencies) {
        Map<B, Set<Inconsistency<B, E>>> boxesWithUnexpectedLine = new LinkedHashMap<>();
        for (var inconsistency : inconsistencies) {
            if (inconsistency.getType() == InconsistencyType.UNEXPECTED_LINE) {
                boxesWithUnexpectedLine.computeIfAbsent(inconsistency.getBox(), k -> new LinkedHashSet<>()).add(inconsistency);
            }
        }
        return boxesWithUnexpectedLine;
    }

    @Override
    public B getOtherBox() {
        return this.expectedTarget;
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new LineInversion<>(boxMapper.apply(this.getBox()), boxMapper.apply(this.getOtherBox()));
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
        LineInversion<?, ?> that = (LineInversion<?, ?>) object;
        return Objects.equals(this.expectedTarget, that.expectedTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.expectedTarget);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("expectedTarget", this.expectedTarget).toString();
    }
}
