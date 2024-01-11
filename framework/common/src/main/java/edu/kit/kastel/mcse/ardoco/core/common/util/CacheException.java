/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

/**
 * Exception that can occur during read or write operations of a {@link FileBasedCache}
 */
public class CacheException extends Exception {
    /**
     * Constructor for cache exception
     *
     * @param cause the cause
     */
    public CacheException(Throwable cause) {
        super(cause);
    }
}
