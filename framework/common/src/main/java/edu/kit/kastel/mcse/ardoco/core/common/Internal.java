/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class as only for internal use. Classes and methods that are marked with this annotation are
 * subject of change.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR })
public @interface Internal {
}
