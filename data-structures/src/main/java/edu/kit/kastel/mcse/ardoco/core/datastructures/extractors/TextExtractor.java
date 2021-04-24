package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

public abstract class TextExtractor extends Extractor {

	protected ITextState textState;

	@Override
	public TextExtractor create(AgentDatastructure data, Configuration config) {

		if (null == data.getTextState()) {
			throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
		}
		return create(data.getTextState(), config);
	}

	public abstract TextExtractor create(ITextState textState, Configuration config);

	protected TextExtractor(DependencyType dependencyType, ITextState textState) {
		super(dependencyType);
		this.textState = textState;
	}
}
