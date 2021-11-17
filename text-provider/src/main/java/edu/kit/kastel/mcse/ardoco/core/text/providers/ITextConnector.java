package edu.kit.kastel.mcse.ardoco.core.text.providers;

import edu.kit.kastel.mcse.ardoco.core.text.IText;

/**
 * The Interface ITextConnector defines a provider for annotated texts.
 */
@FunctionalInterface
public interface ITextConnector {

    /**
     * Gets the annotated text.
     *
     * @return the annotated text
     */
    IText getAnnotatedText();
}
