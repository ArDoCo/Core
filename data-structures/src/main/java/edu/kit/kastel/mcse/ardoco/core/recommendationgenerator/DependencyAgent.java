package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import edu.kit.kastel.mcse.ardoco.core.common.Agent;
import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

import java.util.Objects;

public abstract class DependencyAgent extends Agent {

    protected IText text;
    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;

    protected DependencyAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    protected DependencyAgent(Class<? extends Configuration> configType, IText text,
                              ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(configType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

    @Override
    protected final DependencyAgent createInternal(AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getText());
        Objects.requireNonNull(data.getTextState());
        Objects.requireNonNull(data.getModelState());
        Objects.requireNonNull(data.getRecommendationState());

        return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
    }

    public abstract DependencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
                                                     Configuration config);
}
