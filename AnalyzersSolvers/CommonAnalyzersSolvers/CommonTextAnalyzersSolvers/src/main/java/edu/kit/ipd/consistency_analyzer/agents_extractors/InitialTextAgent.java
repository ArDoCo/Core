package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Loader;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.TextAgent;
import edu.kit.ipd.consistency_analyzer.agents_extractors.extractors.IExtractor;
import edu.kit.ipd.consistency_analyzer.agents_extractors.extractors.TextExtractor;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

@MetaInfServices(TextAgent.class)
public class InitialTextAgent extends TextAgent {

	private Logger logger = Logger.getLogger(getName());

	private List<IExtractor> extractors = new ArrayList<>();

	public InitialTextAgent() {
		super(DependencyType.TEXT);
	}

	public InitialTextAgent(IText text, ITextState textState) {
		this(text, textState, GenericTextConfig.DEFAULT_CONFIG);
	}

	public InitialTextAgent(IText text, ITextState textState, GenericTextConfig config) {
		super(DependencyType.TEXT, text, textState);
		initializeAgents(config.textExtractors);
	}

	@Override
	public TextAgent create(IText text, ITextState textExtractionState) {
		return new InitialTextAgent(text, textExtractionState);
	}

	@Override
	public void exec() {
		for (IWord word : text.getWords()) {
			for (IExtractor extractor : extractors) {
				extractor.exec(word);
			}
		}
	}

	/**
	 * Initializes graph dependent analyzers and solvers
	 */

	private void initializeAgents(List<String> extractorList) {

		Map<String, TextExtractor> loadedExtractors = Loader.loadLoadable(TextExtractor.class);

		for (String textExtractor : extractorList) {
			if (!loadedExtractors.containsKey(textExtractor)) {
				throw new IllegalArgumentException("TextAgent " + textExtractor + " not found");
			}
			extractors.add(loadedExtractors.get(textExtractor).create(textState));
		}
	}

}
