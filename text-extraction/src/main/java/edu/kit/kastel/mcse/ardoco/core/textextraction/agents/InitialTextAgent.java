/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.IExtractor;
import edu.kit.kastel.mcse.ardoco.core.common.Loader;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtractionExtractor;

/**
 * The Class InitialTextAgent defines the agent that executes the extractors for the text stage.
 */
@MetaInfServices(TextAgent.class)
public class InitialTextAgent extends TextAgent {

    private MutableList<IExtractor> extractors = Lists.mutable.empty();

    /**
     * Instantiates a new initial text agent.
     */
    public InitialTextAgent() {
        super(GenericTextConfig.class);
    }

    private InitialTextAgent(IText text, ITextState textState, GenericTextConfig config) {
        super(GenericTextConfig.class, text, textState);
        initializeAgents(config.textExtractors, config);
    }

    @Override
    public TextAgent create(IText text, ITextState textExtractionState, Configuration config) {
        return new InitialTextAgent(text, textExtractionState, (GenericTextConfig) config);
    }

    @Override
    public void exec() {
        for (IWord word : text.getWords()) {
            for (IExtractor extractor : extractors) {
                extractor.exec(word);
            }
        }
    }

    private void initializeAgents(ImmutableList<String> extractorList, GenericTextConfig config) {
        Map<String, TextExtractionExtractor> loadedExtractors = Loader.loadLoadable(TextExtractionExtractor.class);

        for (String textExtractor : extractorList) {
            if (!loadedExtractors.containsKey(textExtractor)) {
                throw new IllegalArgumentException("TextAgent " + textExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(textExtractor).create(textState, config));
        }
    }
}
