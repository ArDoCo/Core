/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.stage;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;

public abstract class AbstractExecutionStage extends Pipeline {
    protected AbstractExecutionStage(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
