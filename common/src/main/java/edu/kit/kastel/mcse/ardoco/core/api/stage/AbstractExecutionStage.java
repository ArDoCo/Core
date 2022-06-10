/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.stage;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;

public abstract class AbstractExecutionStage extends Pipeline {
    public AbstractExecutionStage(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
    }
}
