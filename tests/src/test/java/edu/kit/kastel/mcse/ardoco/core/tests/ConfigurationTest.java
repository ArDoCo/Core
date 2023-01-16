/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import edu.kit.kastel.informalin.framework.configuration.AbstractConfigurable;
import edu.kit.kastel.informalin.framework.configuration.Configurable;

/**
 * This test class deals with the configurations.
 *
 * @see AbstractConfigurable
 */
class ConfigurationTest {

    @Test
    void testValidityOfConfigurableFields() throws Exception {
        var reflectAccess = new Reflections("edu.kit.kastel.mcse.ardoco");
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> c.getPackageName().startsWith("edu.kit.kastel.mcse.ardoco"))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();

        for (var clazz : classesThatMayBeConfigured) {
            List<Field> configurableFields = new ArrayList<>();
            findImportantFields(clazz, configurableFields);

            for (var field : configurableFields) {
                int modifiers = field.getModifiers();
                Assertions.assertFalse(Modifier.isFinal(modifiers), "Field " + field.getName() + "@" + field.getDeclaringClass()
                        .getSimpleName() + " is final!");
                Assertions.assertFalse(Modifier.isStatic(modifiers), "Field " + field.getName() + "@" + field.getDeclaringClass()
                        .getSimpleName() + " is static!");
            }
        }
    }

    private void findImportantFields(Class<?> clazz, List<Field> fields) {
        if (clazz == Object.class || clazz == AbstractConfigurable.class) {
            return;
        }

        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Configurable.class)) {
                fields.add(field);
            }
        }
        findImportantFields(clazz.getSuperclass(), fields);
    }

}
