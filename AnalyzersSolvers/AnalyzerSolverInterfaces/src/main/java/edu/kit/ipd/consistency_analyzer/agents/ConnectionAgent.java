package edu.kit.ipd.consistency_analyzer.agents;

import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

public abstract class ConnectionAgent extends Agent {

	protected IText text;
	protected ITextState textState;
	protected IModelState modelState;
	protected IRecommendationState recommendationState;
	protected IConnectionState connectionState;

	@Override
	public ConnectionAgent create(AgentDatastructure data) {
		if (null == data.getText() || null == data.getTextState() || null == data.getModelState() || null == data.getRecommendationState() || null == data.getConnectionState()) {
			throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
		}
		return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState());
	}

	public abstract ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState);

	/**
	 * Creates a new agent.
	 *
	 * @param dependencyType      the dependencies of the agent
	 * @param graph               the PARSE graph to look up
	 * @param textState           the text extraction state to look up
	 * @param modelState          the model extraction state to look up
	 * @param recommendationState the model extraction state to look up
	 * @param connectionState     the connection state to work with
	 */
	protected ConnectionAgent(DependencyType dependencyType, AgentDatastructure data) {
		this(dependencyType, data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState());
	}

	protected ConnectionAgent(DependencyType dependencyType, IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState) {
		super(dependencyType);
		this.text = text;
		this.textState = textState;
		this.modelState = modelState;
		this.recommendationState = recommendationState;
		this.connectionState = connectionState;
	}

	protected ConnectionAgent(DependencyType dependencyType) {
		super(dependencyType);
	}
}
