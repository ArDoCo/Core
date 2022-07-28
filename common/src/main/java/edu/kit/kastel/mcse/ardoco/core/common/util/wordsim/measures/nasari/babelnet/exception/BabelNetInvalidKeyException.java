/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception;

/**
 * Occurs when using an invalid API key to interact with the BabelNet API.
 */
public class BabelNetInvalidKeyException extends BabelNetException {

    public BabelNetInvalidKeyException(String response) {
        super("The response suggests that an invalid babelnet key was used: " + response);
    }

}
