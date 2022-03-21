/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.common;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface Configurable {
    String key() default "";
}
