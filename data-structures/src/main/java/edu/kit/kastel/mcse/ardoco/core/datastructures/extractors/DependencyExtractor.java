package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.*;

public abstract class DependencyExtractor extends Extractor {

    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;

    protected DependencyExtractor(DependencyType dependencyType, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(dependencyType);
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

    @Override
    public Extractor create(AgentDatastructure data, Configuration config) {
        if (null == data.getTextState() || null == data.getModelState() || null == data.getRecommendationState()) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return create(data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
    }

    public abstract DependencyExtractor create(ITextState textState, IModelState modelState, IRecommendationState recommendationState,
                                                   Configuration config);

    @Override
    public void exec(IWord word) {
        System.out.println(word.getText());
    }

    public abstract void exec(INounMapping mapping);
    public abstract void exec(IRecommendedInstance rec);
}
