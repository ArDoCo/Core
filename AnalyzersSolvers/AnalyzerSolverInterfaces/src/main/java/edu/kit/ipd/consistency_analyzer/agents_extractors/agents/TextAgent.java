package edu.kit.ipd.consistency_analyzer.agents_extractors.agents;

import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

public abstract class TextAgent extends Agent {

    protected IText text;
    protected ITextState textState;

    @Override
    public TextAgent create(AgentDatastructure data, Configuration config) {
        if (null == data.getText() || null == data.getTextState()) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return create(data.getText(), data.getTextState(), config);
    }

    public abstract TextAgent create(IText text, ITextState textState, Configuration config);

    public abstract TextAgent create(IText text, ITextState textState);

    /**
     * Creates a new NameTypeRelationAnalyzer
     *
     * @param dependencyType      the dependencies of the analyzer
     * @param graph               PARSE graph which contains the arcs
     * @param textExtractionState the text extraction state
     */
    protected TextAgent(DependencyType dependencyType, AgentDatastructure data) {
        this(dependencyType, data.getText(), data.getTextState());
    }

    protected TextAgent(DependencyType dependencyType, IText text, ITextState textState) {
        super(dependencyType);
        this.text = text;
        this.textState = textState;
    }

    protected TextAgent(DependencyType dependencyType) {
        super(dependencyType);
    }

}
