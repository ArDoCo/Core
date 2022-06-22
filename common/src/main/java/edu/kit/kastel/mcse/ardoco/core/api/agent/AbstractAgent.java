/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;

public abstract class AbstractAgent extends Pipeline implements Agent {

    protected AbstractAgent(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
