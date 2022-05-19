/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception;

/**
 * A babelnet related exception.
 */
public class BabelNetException extends Exception {

    public BabelNetException() {
    }

    public BabelNetException(String message) {
        super(message);
    }

    public BabelNetException(String message, Throwable cause) {
        super(message, cause);
    }

    public BabelNetException(Throwable cause) {
        super(cause);
    }

    public BabelNetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
