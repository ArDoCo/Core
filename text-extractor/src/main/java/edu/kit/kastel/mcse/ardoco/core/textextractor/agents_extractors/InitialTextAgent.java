package edu.kit.kastel.mcse.ardoco.core.textextractor.agents_extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.IExtractor;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.TextExtractor;

@MetaInfServices(TextAgent.class)
public class InitialTextAgent extends TextAgent {

	private List<IExtractor> extractors = new ArrayList<>();

	public InitialTextAgent() {
		super(GenericTextConfig.class);
	}

	private InitialTextAgent(IText text, ITextState textState, GenericTextConfig config) {
		super(DependencyType.TEXT, GenericTextConfig.class, text, textState);
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

	private void initializeAgents(List<String> extractorList, GenericTextConfig config) {
		Map<String, TextExtractor> loadedExtractors = Loader.loadLoadable(TextExtractor.class);

		for (String textExtractor : extractorList) {
			if (!loadedExtractors.containsKey(textExtractor)) {
				throw new IllegalArgumentException("TextAgent " + textExtractor + " not found");
			}
			extractors.add(loadedExtractors.get(textExtractor).create(textState, config));
		}
	}
}
