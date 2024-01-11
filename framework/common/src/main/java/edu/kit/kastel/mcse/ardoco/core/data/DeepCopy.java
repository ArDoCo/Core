/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks the method or parameter as using deep copies. If a method is annotated as deep copy, it only returns deep copies that can be freely modified. If a
 * parameter is annotated as deep copy, the method will calculate and use a deep copy of it rather than the object itself.
 */
@Documented
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface DeepCopy {
}
