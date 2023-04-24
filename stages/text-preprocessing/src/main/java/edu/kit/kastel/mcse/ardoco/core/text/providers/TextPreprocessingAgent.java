/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.CoreNLPProvider;

public class TextPreprocessingAgent extends PipelineAgent {

    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    /**
     * Instantiates a new initial text agent.
     *
     * @param data the {@link DataRepository}
     */
    public TextPreprocessingAgent(DataRepository data) {
        super(TextPreprocessingAgent.class.getSimpleName(), data);
        informants = List.of(new CoreNLPProvider(data));
        enabledInformants = informants.stream().map(AbstractPipelineStep::getId).toList();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }
}
