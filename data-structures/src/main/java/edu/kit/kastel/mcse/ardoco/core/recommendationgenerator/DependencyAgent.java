/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.Agent;
import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

public abstract class DependencyAgent extends Agent {

    protected IText text;
    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;

    protected DependencyAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    protected DependencyAgent(Class<? extends Configuration> configType, IText text, ITextState textState, IModelState modelState,
            IRecommendationState recommendationState) {
        super(configType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

    @Override
    protected final DependencyAgent createInternal(String modelId, AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getText());
        Objects.requireNonNull(data.getTextState());
        Objects.requireNonNull(data.getModelState(modelId));
        Objects.requireNonNull(data.getRecommendationState(modelId));

        return this.create(data.getText(), data.getTextState(), data.getModelState(modelId), data.getRecommendationState(modelId), config);
    }

    public abstract DependencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config);
}
