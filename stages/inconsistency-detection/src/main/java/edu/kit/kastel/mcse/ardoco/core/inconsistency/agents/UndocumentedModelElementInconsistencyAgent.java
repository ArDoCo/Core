/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.UndocumentedModelElementInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This agent analyses the model to find elements within the model that are not documented in the text. For this, it uses the
 * {@link UndocumentedModelElementInconsistencyInformant}. See it for more information about configuration options.
 */
public class UndocumentedModelElementInconsistencyAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public UndocumentedModelElementInconsistencyAgent(DataRepository dataRepository) {
        super(UndocumentedModelElementInconsistencyAgent.class.getSimpleName(), dataRepository,
                List.of(new UndocumentedModelElementInconsistencyInformant(dataRepository)));
        enabledInformants = getInformants().stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
