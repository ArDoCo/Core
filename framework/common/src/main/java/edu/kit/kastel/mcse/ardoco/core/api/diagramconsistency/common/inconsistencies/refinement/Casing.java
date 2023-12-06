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
 * Indicates that some box texts use different casing conventions that the model.
 *
 * @param <B>
 *            The type of the box.
 * @param <E>
 *            The type of the entity.
 */
@Deterministic
public class Casing<B, E> extends Inconsistency<B, E> {
    private final List<B> boxes = new ArrayList<>();
    private final List<String> expectedNames = new ArrayList<>();

    protected Casing(List<B> boxes, List<String> expectedNames) {
        super(null, null);
        this.boxes.addAll(boxes);
        this.expectedNames.addAll(expectedNames);
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

        List<B> boxes = new ArrayList<>();
        List<String> expectedNames = new ArrayList<>();

        var nameInconsistencies = getAllNameInconsistencies(newInconsistencies);
        for (var nameInconsistency : nameInconsistencies) {
            String actualName = Objects.requireNonNull(nameInconsistency.getActualName());
            String expectedName = Objects.requireNonNull(nameInconsistency.getExpectedName());

            if (actualName.equalsIgnoreCase(expectedName)) {
                boxes.add(Objects.requireNonNull(nameInconsistency.getBox()));
                expectedNames.add(expectedName);

                newInconsistencies.remove(nameInconsistency);
            }
        }

        if (boxes.size() > 1) {
            newInconsistencies.add(new Casing<>(boxes, expectedNames));
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
        return new Casing<>(this.boxes.stream().map(boxMapper).toList(), this.expectedNames);
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
        Casing<?, ?> casing = (Casing<?, ?>) object;
        return Objects.equals(this.boxes, casing.boxes) && Objects.equals(this.expectedNames, casing.expectedNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.boxes, this.expectedNames);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("boxes", this.boxes).append("expectedNames", this.expectedNames).toString();
    }
}
