/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception;

/**
 * Occurs when the daily request limit has been reached.
 */
public class BabelNetRequestLimitException extends BabelNetException {

    public BabelNetRequestLimitException(String response) {
        super("Response suggests that the babelnet request limit was reached: " + response);
    }

}
