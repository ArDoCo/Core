/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyState;

/**
 * @see DiagramInconsistencyState
 */
public class DiagramInconsistencyStateImpl implements DiagramInconsistencyState {
    private final Set<Inconsistency> inconsistencies = new HashSet<>();

    @Override
    public <T extends Inconsistency> Set<T> getInconsistencies(Class<T> type) {
        return inconsistencies.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Inconsistency> getInconsistencies() {
        return Set.copyOf(inconsistencies);
    }

    @Override
    public boolean addInconsistency(Inconsistency inconsistency) {
        return inconsistencies.add(inconsistency);
    }
}
