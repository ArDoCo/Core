/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import edu.kit.kastel.mcse.ardoco.core.text.IText;

/**
 * The Interface ITextConnector defines a provider for annotated texts.
 */
public interface ITextConnector {

    /**
     * Gets the annotated text.
     *
     * @return the annotated text
     */
    IText getAnnotatedText();

    /**
     * Gets the annotated text with the given name
     *
     * @param textName name of the text to retrieve
     * @return the annotated text with the given name
     */
    IText getAnnotatedText(String textName);
}
