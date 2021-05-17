/**
 *
 */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;

/**
 * @author Jan Keim
 *
 */
public class InconsistencyState implements IInconsistencyState {

    private List<IInconsistency> inconsistencies = new ArrayList<>();

    /**
     * Add an Inconsistency to this state
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    @Override
    public boolean addInconsistency(IInconsistency inconsistency) {
        return inconsistencies.add(inconsistency);
    }

    /**
     * Returns a list of inconsistencies held by this state
     *
     * @return list of inconsistencies
     */
    @Override
    public List<IInconsistency> getInconsistencies() {
        return inconsistencies;
    }

}
