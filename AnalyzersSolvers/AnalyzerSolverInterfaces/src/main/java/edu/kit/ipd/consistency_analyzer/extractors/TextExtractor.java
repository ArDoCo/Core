package edu.kit.ipd.consistency_analyzer.extractors;

import edu.kit.ipd.consistency_analyzer.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

public abstract class TextExtractor extends Extractor {

    protected ITextState textState;

    @Override
    public TextExtractor create(AgentDatastructure data) {

        if (null == data.getTextState()) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return create(data.getTextState());
    }

    public abstract TextExtractor create(ITextState textState);

    protected TextExtractor(DependencyType dependencyType, ITextState textState) {
        super(dependencyType);
        this.textState = textState;
    }
}
