package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

public abstract class ConnectionAgent extends Agent {

	protected IText text;
	protected ITextState textState;
	protected IModelState modelState;
	protected IRecommendationState recommendationState;
	protected IConnectionState connectionState;

	/**
	 * Prototype Constructor
	 */
	protected ConnectionAgent(Class<? extends Configuration> configType) {
		super(configType);
	}

	protected ConnectionAgent(DependencyType dependencyType, Class<? extends Configuration> configType, IText text, ITextState textState,
			IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState) {
		super(dependencyType, configType);
		this.text = text;
		this.textState = textState;
		this.modelState = modelState;
		this.recommendationState = recommendationState;
		this.connectionState = connectionState;
	}

	@Override
	protected final ConnectionAgent createInternal(AgentDatastructure data, Configuration config) {
		if (data.getText() == null || data.getTextState() == null || data.getModelState() == null || data.getRecommendationState() == null
				|| data.getConnectionState() == null) {
			throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
		}
		return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState(), config);
	}

	public abstract ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
			IConnectionState connectionState, Configuration config);

}
