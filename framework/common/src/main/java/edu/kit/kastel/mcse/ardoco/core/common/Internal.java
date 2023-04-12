/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.lang.annotation.*;

/**
 * Marks a method or class as only for internal use. Classes and methods that are marked with this annotation are
 * subject of change.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Internal {
}
