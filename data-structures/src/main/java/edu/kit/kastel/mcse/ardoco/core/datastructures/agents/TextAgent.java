package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

/**
 * The Class TextAgent.
 */
public abstract class TextAgent extends Agent {

    /** The text. */
    protected IText text;

    /** The text state. */
    protected ITextState textState;

    /**
     * Prototype Constructor.
     *
     * @param configType the configuration type to be used by this agent
     */
    protected TextAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    /**
     * Instantiates a new text agent.
     *
     * @param configType the configuration type to be used by this agen
     * @param text       the text
     * @param textState  the text state
     */
    protected TextAgent(Class<? extends Configuration> configType, IText text, ITextState textState) {
        super(configType);
        this.text = text;
        this.textState = textState;
    }

    @Override
    protected final TextAgent createInternal(AgentDatastructure data, Configuration config) {
        if (data.getText() == null || data.getTextState() == null) {
            throw new IllegalArgumentException("An input of the agent" + getId() + " was null!");
        }
        return create(data.getText(), data.getTextState(), config);
    }

    /**
     * Creates the text agent.
     *
     * @param text      the text
     * @param textState the text state
     * @param config    the config
     * @return the text agent
     */
    public abstract TextAgent create(IText text, ITextState textState, Configuration config);

}
