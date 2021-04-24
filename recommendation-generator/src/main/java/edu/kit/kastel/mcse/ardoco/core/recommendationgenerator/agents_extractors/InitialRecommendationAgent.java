package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.IExtractor;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.RecommendationExtractor;

@MetaInfServices(RecommendationAgent.class)
public class InitialRecommendationAgent extends RecommendationAgent {

	private List<IExtractor> extractors = new ArrayList<>();

	public InitialRecommendationAgent() {
		super(GenericRecommendationConfig.class);
	}

	private InitialRecommendationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
			GenericRecommendationConfig config) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
		initializeAgents(config.recommendationExtractors, config);
	}

	private void initializeAgents(List<String> extractorList, GenericRecommendationConfig config) {
		Map<String, RecommendationExtractor> loadedExtractors = Loader.loadLoadable(RecommendationExtractor.class);

		for (String recommendationExtractor : extractorList) {
			if (!loadedExtractors.containsKey(recommendationExtractor)) {
				throw new IllegalArgumentException("RecommendationExtractor " + recommendationExtractor + " not found");
			}
			extractors.add(loadedExtractors.get(recommendationExtractor).create(textState, modelState, recommendationState, config));
		}

	}

	@Override
	public RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
			Configuration config) {
		return new InitialRecommendationAgent(text, textState, modelState, recommendationState, (GenericRecommendationConfig) config);
	}

	@Override
	public void exec() {

		for (IExtractor extractor : extractors) {
			for (IWord word : text.getWords()) {
				extractor.exec(word);
			}
		}
	}
}
