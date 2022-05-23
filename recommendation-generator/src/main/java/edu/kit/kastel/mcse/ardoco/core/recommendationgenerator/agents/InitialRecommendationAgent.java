/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors.NameTypeExtractor;

/**
 * The Class InitialRecommendationAgent runs all extractors of this stage.
 */
public class InitialRecommendationAgent extends RecommendationAgent {

    private final List<AbstractExtractor<RecommendationAgentData>> extractors = List.of(new NameTypeExtractor());

    @Configurable
    private List<String> enabledExtractors = extractors.stream().map(e -> e.getClass().getSimpleName()).toList();

    /**
     * Prototype constructor.
     */
    public InitialRecommendationAgent() {
        // empty
    }

    @Override
    public void execute(RecommendationAgentData data) {
        var text = data.getText();
        for (var extractor : findByClassName(enabledExtractors, extractors)) {
            for (IWord word : text.getWords()) {
                extractor.exec(data, word);
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
