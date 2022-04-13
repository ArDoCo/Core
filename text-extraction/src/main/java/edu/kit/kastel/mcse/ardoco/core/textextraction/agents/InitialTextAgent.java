/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.ArticleTypeNameExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.InDepArcsExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.NounExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.OutDepArcsExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.SeparatedNamesExtractor;

/**
 * The Class InitialTextAgent defines the agent that executes the extractors for the text stage.
 */
public class InitialTextAgent extends TextAgent {

    private final List<AbstractExtractor<TextAgentData>> extractors = List.of(new NounExtractor(), new InDepArcsExtractor(), new OutDepArcsExtractor(),
            new ArticleTypeNameExtractor(), new SeparatedNamesExtractor());

    @Configurable
    private List<String> enabledExtractors = extractors.stream().map(e -> e.getClass().getSimpleName()).toList();

    /**
     * Instantiates a new initial text agent.
     */
    public InitialTextAgent() {
        // empty
    }

    @Override
    public void execute(TextAgentData data) {
        var text = data.getText();
        for (IWord word : text.getWords()) {
            for (var extractor : findByClassName(enabledExtractors, extractors)) {
                extractor.exec(data, word);
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
