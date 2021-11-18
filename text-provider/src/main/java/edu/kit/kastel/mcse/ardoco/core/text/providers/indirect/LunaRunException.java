package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

/**
 * @author Sebastian Weigelt
 */
public class LunaRunException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 5462825340872288175L;

    public LunaRunException() {
        super();
    }

    public LunaRunException(String message) {
        super(message);
    }

    public LunaRunException(String message, Throwable cause) {
        super(message, cause);
    }

    public LunaRunException(Throwable cause) {
        super(cause);
    }
}
