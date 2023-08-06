/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.CoreNLPProvider;

public class TextPreprocessingAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    /**
     * Instantiates a new initial text agent.
     *
     * @param data the {@link DataRepository}
     */
    public TextPreprocessingAgent(DataRepository data) {
        super(TextPreprocessingAgent.class.getSimpleName(), data, List.of(new CoreNLPProvider(data)));
        enabledInformants = getInformantClassNames();
    }

    /**
     * Creates a {@link CoreNLPProvider} as {@link NlpInformant} and reads the provided text.
     *
     * @param additionalConfigs the additional configuration that should be applied
     * @param dataRepository    the data repository
     * @return a CoreNLPProvider with the provided text read in
     */
    public static TextPreprocessingAgent get(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var textProvider = new TextPreprocessingAgent(dataRepository);
        textProvider.applyConfiguration(additionalConfigs);
        return textProvider;
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
