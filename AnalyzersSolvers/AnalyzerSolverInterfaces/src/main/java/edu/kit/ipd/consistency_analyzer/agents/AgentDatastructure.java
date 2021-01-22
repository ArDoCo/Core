package edu.kit.ipd.consistency_analyzer.agents;

import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

public class AgentDatastructure {

	private IText text;
	private ITextState textState;
	private IModelState modelState;
	private IRecommendationState recommendationState;
	private IConnectionState connectionState;

	public AgentDatastructure() {
	}

	public AgentDatastructure(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState) {
		this.text = text;
		this.textState = textState;
		this.modelState = modelState;
		this.recommendationState = recommendationState;
		this.connectionState = connectionState;
	}

	public IText getText() {
		return text;
	}

	public void setText(IText text) {
		this.text = text;
	}

	public ITextState getTextState() {
		return textState;
	}

	public void setTextState(ITextState textState) {
		this.textState = textState;
	}

	public IModelState getModelState() {
		return modelState;
	}

	public void setModelState(IModelState modelState) {
		this.modelState = modelState;
	}

	public IRecommendationState getRecommendationState() {
		return recommendationState;
	}

	public void setRecommendationState(IRecommendationState recommendationState) {
		this.recommendationState = recommendationState;
	}

	public IConnectionState getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(IConnectionState connectionState) {
		this.connectionState = connectionState;
	}

	public void overwrite(AgentDatastructure newData) {
		text = newData.text;
		textState = newData.textState;
		modelState = newData.modelState;
		recommendationState = newData.recommendationState;
		connectionState = newData.connectionState;
	}

}
