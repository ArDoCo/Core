/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors.PhraseRecommendationExtractor;

public final class PhraseRecommendationAgent extends PipelineAgent {

    private final List<Informant> extractors;

    @Configurable
    private List<String> enabledExtractors;

    public PhraseRecommendationAgent(DataRepository dataRepository) {
        super("PhraseRecommendationAgent", dataRepository);

        extractors = List.of(new PhraseRecommendationExtractor(dataRepository));
        enabledExtractors = extractors.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    public void run() {
        for (var extractor : findByClassName(enabledExtractors, extractors)) {
            this.addPipelineStep(extractor);
        }

        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
