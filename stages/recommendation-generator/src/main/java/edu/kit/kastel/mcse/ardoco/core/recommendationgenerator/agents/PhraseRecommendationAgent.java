/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.informants.CompoundRecommendationInformant;

public final class PhraseRecommendationAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public PhraseRecommendationAgent(DataRepository dataRepository) {
        super(PhraseRecommendationAgent.class.getSimpleName(), dataRepository, List.of(new CompoundRecommendationInformant(dataRepository)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
