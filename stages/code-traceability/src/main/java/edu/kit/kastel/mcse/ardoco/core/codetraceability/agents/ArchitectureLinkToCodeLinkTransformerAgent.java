/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.ArchitectureLinkToCodeLinkTransformerInformant;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This class transforms architecture trace links to code trace links by remapping the architecture ids (which are code ids) to the compilation units.
 */
public class ArchitectureLinkToCodeLinkTransformerAgent extends PipelineAgent {

    @Configurable
    private List<String> enabledInformants;

    public ArchitectureLinkToCodeLinkTransformerAgent(DataRepository dataRepository) {
        super(ArchitectureLinkToCodeLinkTransformerAgent.class.getSimpleName(), dataRepository,
                List.of(new ArchitectureLinkToCodeLinkTransformerInformant(dataRepository)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected void initializeState() {
        // empty
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
