/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.ArchitectureLinkToCodeLinkTransformerInformant;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This class transforms architecture trace links to code trace links by remapping the architecture ids (which are code ids) to the compilation units.
 */
public class ArchitectureLinkToCodeLinkTransformerAgent extends PipelineAgent {
    private final MutableList<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    public ArchitectureLinkToCodeLinkTransformerAgent(DataRepository dataRepository) {
        super(ArchitectureLinkToCodeLinkTransformerAgent.class.getSimpleName(), dataRepository);

        informants = Lists.mutable.of(new ArchitectureLinkToCodeLinkTransformerInformant(dataRepository));
        enabledInformants = informants.collect(Informant::getId);
    }

    @Override
    protected void initializeState() {
        // empty
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }
}
