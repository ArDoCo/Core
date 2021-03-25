package edu.kit.ipd.consistency_analyzer.agents_extractors.agents;

import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

public abstract class RecommendationAgent extends Agent {

	protected IText text;
	protected ITextState textState;
	protected IModelState modelState;
	protected IRecommendationState recommendationState;

	/**
	 * Prototype Constructor
	 */
	protected RecommendationAgent(Class<? extends Configuration> configType) {
		super(configType);
	}

	protected RecommendationAgent(//
			DependencyType dependencyType, Class<? extends Configuration> configType, IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
		super(dependencyType, configType);
		this.text = text;
		this.textState = textState;
		this.modelState = modelState;
		this.recommendationState = recommendationState;
	}

	@Override
	protected final RecommendationAgent createInternal(AgentDatastructure data, Configuration config) {
		if (data.getText() == null || data.getTextState() == null || data.getModelState() == null || data.getRecommendationState() == null) {
			throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
		}
		return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
	}

	public abstract RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, Configuration config);

}
