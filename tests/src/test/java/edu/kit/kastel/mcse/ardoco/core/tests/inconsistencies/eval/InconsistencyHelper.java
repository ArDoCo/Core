package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;

public final class InconsistencyHelper {
    private InconsistencyHelper() {
        throw new IllegalAccessError();
    }

    public static InconsistencyDiff getDiff(IInconsistencyState original, IInconsistencyState newState) {
        return new InconsistencyDiff(original, newState);
    }

    public static final class InconsistencyDiff {
        private IInconsistencyState original;
        private IInconsistencyState newState;

        InconsistencyDiff(IInconsistencyState original, IInconsistencyState newState) {
            this.original = original;
            this.newState = newState;
        }

        public ImmutableList<IInconsistency> getNewInconsistencies() {
            return newState.getInconsistencies().select(i -> !this.original.getInconsistencies().contains(i));
        }

        public ImmutableList<IInconsistency> getRemovedInconsistencies() {
            return original.getInconsistencies().select(i -> !this.newState.getInconsistencies().contains(i));
        }

        public ImmutableList<IInconsistency> getOldInconsistencies() {
            return newState.getInconsistencies().select(i -> this.original.getInconsistencies().contains(i));
        }
    }

}
