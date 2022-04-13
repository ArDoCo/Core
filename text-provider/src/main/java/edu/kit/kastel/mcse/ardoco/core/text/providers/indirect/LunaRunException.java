/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import java.io.Serial;

/**
 * @author Sebastian Weigelt
 */
public class LunaRunException extends RuntimeException {

    @Serial
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
