/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.UndocumentedModelElementInconsistencyInformant;

/**
 * This agent analyses the model to find elements within the model that are not documented in the text.
 * For this, it uses the {@link UndocumentedModelElementInconsistencyInformant}. See it for more information about configuration options.
 */
public class UndocumentedModelElementInconsistencyAgent extends PipelineAgent {

    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    public UndocumentedModelElementInconsistencyAgent(DataRepository dataRepository) {
        super(UndocumentedModelElementInconsistencyAgent.class.getSimpleName(), dataRepository);

        informants = List.of(new UndocumentedModelElementInconsistencyInformant(dataRepository));
        enabledInformants = informants.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
