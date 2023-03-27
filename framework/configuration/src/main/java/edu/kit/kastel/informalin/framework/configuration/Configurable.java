/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.configuration;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface Configurable {
    String key() default "";
}
