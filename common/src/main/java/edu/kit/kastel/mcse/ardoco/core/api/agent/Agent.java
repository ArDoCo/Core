/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;

public abstract class Agent extends Pipeline implements IAgent {

    protected Agent(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
