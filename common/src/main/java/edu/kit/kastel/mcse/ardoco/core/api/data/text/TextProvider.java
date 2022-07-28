/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

/**
 * The Interface ITextConnector defines a provider for annotated texts.
 */
public interface TextProvider {

    /**
     * Gets the annotated text.
     *
     * @return the annotated text
     */
    Text getAnnotatedText();

    /**
     * Gets the annotated text with the given name
     *
     * @param textName name of the text to retrieve
     * @return the annotated text with the given name
     */
    Text getAnnotatedText(String textName);
}
