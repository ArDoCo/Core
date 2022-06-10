/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.data.DataRepository;

public abstract class InconsistencyAgent extends AbstractAgent {

    protected InconsistencyAgent(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
