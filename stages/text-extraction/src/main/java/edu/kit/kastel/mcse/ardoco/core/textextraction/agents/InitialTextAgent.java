/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.InDepArcsInformant;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.NounInformant;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.OutDepArcsInformant;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.SeparatedNamesInformant;

/**
 * The Class InitialTextAgent defines the agent that executes the extractors for the text stage.
 */
public class InitialTextAgent extends PipelineAgent {

    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    /**
     * Instantiates a new initial text agent.
     *
     * @param data the {@link DataRepository}
     */
    public InitialTextAgent(DataRepository data) {
        super(InitialTextAgent.class.getSimpleName(), data);
        informants = List.of(new NounInformant(data), new InDepArcsInformant(data), new OutDepArcsInformant(data), new SeparatedNamesInformant(data));
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
