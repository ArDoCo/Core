package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ICopyable;

public interface IInconsistency extends ICopyable<IInconsistency> {

    /**
     * Returns the reason why there is an inconsistency
     *
     * @return The reason of inconsistency
     */
    String getReason();

}
