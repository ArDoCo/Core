package edu.kit.ipd.consistency_analyzer.agents_extractors.extractors;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

public abstract class RecommendationExtractor extends Extractor {

    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;

    @Override
    public RecommendationExtractor create(AgentDatastructure data) {

        if (null == data.getTextState() || null == data.getModelState() || null == data.getRecommendationState()) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return create(data.getTextState(), data.getModelState(), data.getRecommendationState());
    }

    public abstract RecommendationExtractor create(ITextState textState, IModelState modelState, IRecommendationState recommendationState);

    protected RecommendationExtractor(DependencyType dependencyType, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(dependencyType);
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

}
