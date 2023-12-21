/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.InconsistencyType;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Indicates that the name of a box has been extended with unexpected text, e.g. a description.
 *
 * @param <B>
 *            The type of the box.
 * @param <E>
 *            The type of the entity.
 */
@Deterministic
public class NameExtension<B, E> extends Inconsistency<B, E> {
    private final String expectedName;

    protected NameExtension(B box, E entity, String expectedName) {
        super(box, entity);
        this.expectedName = expectedName;
    }

    /**
     * Discovers inconsistencies of this type in a set of basic inconsistencies.
     *
     * @param inconsistencies
     *                           The list of inconsistencies, will be modified.
     * @param boxNameProvider
     *                           A function that provides the name of a box.
     * @param entityNameProvider
     *                           A function that provides the name of an entity.
     * @param <B>
     *                           The type used to represent boxes.
     * @param <E>
     *                           The type used to represent entities.
     * @return The list of inconsistencies, modified to include the newly discovered ones and without the ones that were
     *         used to discover new ones.
     */
    public static <B, E> List<Inconsistency<B, E>> discover(List<Inconsistency<B, E>> inconsistencies, Function<B, String> boxNameProvider,
            Function<E, String> entityNameProvider) {
        Set<Inconsistency<B, E>> newInconsistencies = new LinkedHashSet<>(inconsistencies);

        List<Inconsistency<B, E>> nameInconsistencies = getAllNameInconsistencies(newInconsistencies);
        for (var nameInconsistency : nameInconsistencies) {
            Objects.requireNonNull(nameInconsistency.getActualName());
            Objects.requireNonNull(nameInconsistency.getExpectedName());

            if (nameInconsistency.getActualName().contains(nameInconsistency.getExpectedName())) {
                newInconsistencies.add(new NameExtension<>(nameInconsistency.getBox(), nameInconsistency.getEntity(), nameInconsistency.getExpectedName()));
                newInconsistencies.remove(nameInconsistency);
            }
        }

        List<Inconsistency<B, E>> missingBoxInconsistencies = getAllMissingBoxInconsistencies(newInconsistencies);
        List<Inconsistency<B, E>> unexpectedBoxInconsistencies = getAllUnexpectedBoxInconsistencies(newInconsistencies);
        for (var missingBoxInconsistency : missingBoxInconsistencies) {
            for (var unexpectedBoxInconsistency : unexpectedBoxInconsistencies) {
                Objects.requireNonNull(missingBoxInconsistency.getEntity());
                Objects.requireNonNull(unexpectedBoxInconsistency.getBox());

                String missingName = entityNameProvider.apply(missingBoxInconsistency.getEntity());
                String unexpectedName = boxNameProvider.apply(unexpectedBoxInconsistency.getBox());

                if (unexpectedName.contains(missingName)) {
                    newInconsistencies.add(new NameExtension<>(unexpectedBoxInconsistency.getBox(), missingBoxInconsistency.getEntity(), missingName));
                    newInconsistencies.remove(missingBoxInconsistency);
                    newInconsistencies.remove(unexpectedBoxInconsistency);
                }
            }
        }

        return new ArrayList<>(newInconsistencies);
    }

    private static <B, E> List<Inconsistency<B, E>> getAllNameInconsistencies(Set<Inconsistency<B, E>> inconsistencies) {
        return inconsistencies.stream().filter(inconsistency -> inconsistency.getType() == InconsistencyType.NAME_INCONSISTENCY).toList();
    }

    private static <B, E> List<Inconsistency<B, E>> getAllMissingBoxInconsistencies(Set<Inconsistency<B, E>> inconsistencies) {
        return inconsistencies.stream().filter(inconsistency -> inconsistency.getType() == InconsistencyType.MISSING_BOX).toList();
    }

    private static <B, E> List<Inconsistency<B, E>> getAllUnexpectedBoxInconsistencies(Set<Inconsistency<B, E>> inconsistencies) {
        return inconsistencies.stream().filter(inconsistency -> inconsistency.getType() == InconsistencyType.UNEXPECTED_BOX).toList();
    }

    @Override
    public <R, M> Inconsistency<R, M> map(Function<B, R> boxMapper, Function<E, M> entityMapper) {
        return new NameExtension<>(boxMapper.apply(this.getBox()), entityMapper.apply(this.getEntity()), this.expectedName);
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
        NameExtension<?, ?> that = (NameExtension<?, ?>) object;
        return Objects.equals(this.expectedName, that.expectedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.expectedName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("expectedName", this.expectedName).toString();
    }
}
