/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants.ProjectNameInformant;

/**
 * This agent should look for {@link edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance RecommendedInstances} that contain the
 * project's name and "filters" them by adding a heavy negative probability, thus making the
 * {@link edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance} extremely improbable.
 */
public class ProjectNameFilterAgent extends PipelineAgent {
    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    /**
     * Create the agent.
     */
    public ProjectNameFilterAgent(DataRepository dataRepository) {
        super("ProjectNameFilterAgent", dataRepository);

        informants = List.of(new ProjectNameInformant(dataRepository));
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
