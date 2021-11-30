/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;


import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.Extractor;
import java.util.Objects;

/**
 * The Class TextExtractor is the base class for extractors in the text processing stage.
 */
public abstract class TextExtractionExtractor extends Extractor {

    /** The text state. */
    protected ITextState textState;

    /**
     * Instantiates a new text extractor.
     *
     * @param textState the text state
     */
    protected TextExtractionExtractor(ITextState textState) {
        this.textState = textState;
    }

    @Override
    public final TextExtractionExtractor create(AgentDatastructure data, Configuration config) {
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
    public abstract TextExtractionExtractor create(ITextState textState, Configuration config);

}
