/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.UndocumentedModelElementInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This agent analyses the model to find elements within the model that are not documented in the text. For this, it uses the
 * {@link UndocumentedModelElementInconsistencyInformant}. See it for more information about configuration options.
 */
public class UndocumentedModelElementInconsistencyAgent extends PipelineAgent {

    public UndocumentedModelElementInconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new UndocumentedModelElementInconsistencyInformant(dataRepository)), UndocumentedModelElementInconsistencyAgent.class.getSimpleName(),
                dataRepository);
    }
}
