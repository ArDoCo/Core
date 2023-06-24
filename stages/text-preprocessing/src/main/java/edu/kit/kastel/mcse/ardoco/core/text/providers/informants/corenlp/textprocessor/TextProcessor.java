package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;

public interface TextProcessor {

    /**
     * processes and annotates a given text
     * @param inputText the input text
     * @return          the annotated text
     */
    Text processText(String inputText);

}
