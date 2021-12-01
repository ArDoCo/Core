/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * @author Jan Keim
 *
 */
public class InconsistencyState implements IInconsistencyState {

    private MutableList<IInconsistency> inconsistencies;

    public InconsistencyState() {
        inconsistencies = Lists.mutable.empty();
    }

    /**
     * Add an Inconsistency to this state. Does not add duplicate inconsistencies.
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    @Override
    public boolean addInconsistency(IInconsistency inconsistency) {
        if (!inconsistencies.contains(inconsistency)) {
            return inconsistencies.add(inconsistency);
        }
        return false;
    }

    /**
     * Returns a list of inconsistencies held by this state
     *
     * @return list of inconsistencies
     */
    @Override
    public ImmutableList<IInconsistency> getInconsistencies() {
        return inconsistencies.toImmutable();
    }

    @Override
    public InconsistencyState createCopy() {
        var newInconsistencyState = new InconsistencyState();
        newInconsistencyState.inconsistencies = inconsistencies.collect(IInconsistency::createCopy);
        return newInconsistencyState;
    }

}
