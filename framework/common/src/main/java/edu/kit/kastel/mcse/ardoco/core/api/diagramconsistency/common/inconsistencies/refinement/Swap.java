/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jgrapht.alg.util.UnorderedPair;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Indicates that two boxes are (partially) swapped.
 *
 * @param <B>
 *            The type of the box.
 * @param <E>
 *            The type of the entity.
 */
@Deterministic
public class Swap<B, E> extends Inconsistency<B, E> {
    private final B otherBox;
    private final String otherExpectedName;

    protected Swap(B box, E entity, B otherBox, String otherExpectedName) {
        super(box, entity);
        this.otherBox = otherBox;
        this.otherExpectedName = otherExpectedName;
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

        Set<UnorderedPair<Inconsistency<B, E>, Inconsistency<B, E>>> done = new LinkedHashSet<>();

        var nameInconsistencies = getAllNameInconsistencies(newInconsistencies);
        for (var first : nameInconsistencies) {
            for (var second : nameInconsistencies) {
                if (first == second || done.contains(new UnorderedPair<>(first, second)))
                    continue;
                done.add(new UnorderedPair<>(first, second));

                String firstExpectedName = Objects.requireNonNull(first.getExpectedName());
                String secondActualName = Objects.requireNonNull(second.getActualName());
                String secondExpectedName = Objects.requireNonNull(second.getExpectedName());

                if (firstExpectedName.equals(secondActualName)) {
                    newInconsistencies.add(new Swap<>(first.getBox(), first.getEntity(), second.getBox(), secondExpectedName));

                    newInconsistencies.remove(first);
                    newInconsistencies.remove(second);
                }
            }
        }

        return new ArrayList<>(newInconsistencies);
    }

    private static <B, E> List<Inconsistency<B, E>> getAllNameInconsistencies(Set<Inconsistency<B, E>> inconsistencies) {
        return inconsistencies.stream().filter(inconsistency -> inconsistency.getType() == InconsistencyType.NAME_INCONSISTENCY).toList();
    }

    @Override
    public B getOtherBox() {
        return null;
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new Swap<>(boxMapper.apply(this.getBox()), entityMapper.apply(this.getEntity()), boxMapper.apply(this.otherBox), this.otherExpectedName);
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
        Swap<?, ?> casing = (Swap<?, ?>) object;
        return Objects.equals(this.otherBox, casing.otherBox) && Objects.equals(this.otherExpectedName, casing.otherExpectedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.otherBox, this.otherExpectedName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("otherBox", this.otherBox).append("otherExpectedName", this.otherExpectedName).toString();
    }
}
