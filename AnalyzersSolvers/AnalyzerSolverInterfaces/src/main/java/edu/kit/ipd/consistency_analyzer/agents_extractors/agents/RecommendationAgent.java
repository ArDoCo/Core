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

    @Override
    public RecommendationAgent create(AgentDatastructure data, Configuration config) {
        if (null == data.getText() || null == data.getTextState() || null == data.getModelState() || null == data.getRecommendationState()) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
    }

    public abstract RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config);

    public abstract RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState);

    protected RecommendationAgent(DependencyType dependencyType, AgentDatastructure data) {
        this(dependencyType, data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState());
    }

    protected RecommendationAgent(//
            DependencyType dependencyType, IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(dependencyType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

    protected RecommendationAgent(DependencyType dependencyType) {
        super(dependencyType);
    }

}
