package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

public abstract class RecommendationExtractor extends Extractor {

	protected ITextState textState;
	protected IModelState modelState;
	protected IRecommendationState recommendationState;

	@Override
	public RecommendationExtractor create(AgentDatastructure data, Configuration config) {

		if (null == data.getTextState() || null == data.getModelState() || null == data.getRecommendationState()) {
			throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
		}
		return create(data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
	}

	public abstract RecommendationExtractor create(ITextState textState, IModelState modelState, IRecommendationState recommendationState,
			Configuration config);

	protected RecommendationExtractor(DependencyType dependencyType, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
		super(dependencyType);
		this.textState = textState;
		this.modelState = modelState;
		this.recommendationState = recommendationState;
	}

}
