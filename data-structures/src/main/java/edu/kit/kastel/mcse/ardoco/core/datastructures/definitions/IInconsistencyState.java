package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IState;

/**
 * @author Jan Keim
 *
 */
public interface IInconsistencyState extends IState<IInconsistencyState> {

    /**
     * Returns a list of inconsistencies held by this state
     *
     * @return list of inconsistencies
     */
    ImmutableList<IInconsistency> getInconsistencies();

    /**
     * Add an Inconsistency to this state
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    boolean addInconsistency(IInconsistency inconsistency);

}
