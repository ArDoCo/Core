package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

/**
 * The Class TextExtractor is the base class for extractors in the text processing stage.
 */
public abstract class TextExtractor extends Extractor {

    /** The text state. */
    protected ITextState textState;

    /**
     * Instantiates a new text extractor.
     *
     * @param textState the text state
     */
    protected TextExtractor(ITextState textState) {
        this.textState = textState;
    }

    @Override
    public final TextExtractor create(AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getTextState());

        return create(data.getTextState(), config);
    }

    /**
     * Creates the extractor.
     *
     * @param textState the text state
     * @param config    the config
     * @return the text extractor
     */
    public abstract TextExtractor create(ITextState textState, Configuration config);

}
