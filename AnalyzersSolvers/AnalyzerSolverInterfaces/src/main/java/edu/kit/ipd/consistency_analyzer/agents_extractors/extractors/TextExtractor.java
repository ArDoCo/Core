package edu.kit.ipd.consistency_analyzer.agents_extractors.extractors;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

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
