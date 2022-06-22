/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.data.DataRepository;

public abstract class ConnectionAgent extends Agent {

    protected ConnectionAgent(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
