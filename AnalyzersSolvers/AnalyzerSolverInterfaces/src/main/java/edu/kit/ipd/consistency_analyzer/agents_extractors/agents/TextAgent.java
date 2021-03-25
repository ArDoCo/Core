package edu.kit.ipd.consistency_analyzer.agents_extractors.agents;

import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

public abstract class TextAgent extends Agent {

	protected IText text;
	protected ITextState textState;

	/**
	 * Prototype Constructor
	 */
	protected TextAgent(Class<? extends Configuration> configType) {
		super(configType);
	}

	protected TextAgent(DependencyType dependencyType, Class<? extends Configuration> configType, IText text, ITextState textState) {
		super(dependencyType, configType);
		this.text = text;
		this.textState = textState;
	}

	@Override
	protected final TextAgent createInternal(AgentDatastructure data, Configuration config) {
		if (data.getText() == null || data.getTextState() == null) {
			throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
		}
		return create(data.getText(), data.getTextState(), config);
	}

	public abstract TextAgent create(IText text, ITextState textState, Configuration config);

}
