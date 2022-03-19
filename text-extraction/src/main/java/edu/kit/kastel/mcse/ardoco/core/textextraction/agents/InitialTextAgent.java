/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.*;

/**
 * The Class InitialTextAgent defines the agent that executes the extractors for the text stage.
 */
public class InitialTextAgent extends TextAgent {

    private MutableList<AbstractExtractor<TextAgentData>> extractors = Lists.mutable.of(new NounExtractor(), new InDepArcsExtractor(),
            new OutDepArcsExtractor(), new ArticleTypeNameExtractor(), new SeparatedNamesExtractor());

    @Configurable
    private List<String> enabledExtractors = extractors.collect(e -> e.getClass().getSimpleName());

    /**
     * Instantiates a new initial text agent.
     */
    public InitialTextAgent() {
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
