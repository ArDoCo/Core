/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
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
    @Configurable
    private List<String> enabledInformants;

    /**
     * Instantiates a new initial text agent.
     *
     * @param data the {@link DataRepository}
     */
    public InitialTextAgent(DataRepository data) {
        super(InitialTextAgent.class.getSimpleName(), data,
                List.of(new NounInformant(data), new InDepArcsInformant(data), new OutDepArcsInformant(data), new SeparatedNamesInformant(data)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }

}
