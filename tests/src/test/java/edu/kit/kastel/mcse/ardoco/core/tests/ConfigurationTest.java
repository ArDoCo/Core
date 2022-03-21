/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import edu.kit.kastel.mcse.ardoco.core.api.common.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;

/**
 * This test class deals with the configurations.
 *
 * @author Dominik Fuchss
 * @see AbstractConfigurable
 */
public class ConfigurationTest {
    /**
     * This test verifies that all configurable values are able to be configured. It also prints all configurable values
     * as they should be contained in a configuration file.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void showCurrentConfiguration() throws Exception {
        Map<String, String> configs = new TreeMap<>();
        var reflectAccess = new Reflections("edu.kit.kastel.mcse.ardoco");
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .toList();
        for (var clazz : classesThatMayBeConfigured)
            processConfigurationOfClass(configs, clazz);
        Assertions.assertFalse(configs.isEmpty());

        System.out.println("-".repeat(50));
        System.out.println("Current Default Configuration");
        System.out.println(
                configs.entrySet().stream().map(e -> e.getKey() + AbstractConfigurable.KEY_VALUE_CONNECTOR + e.getValue()).collect(Collectors.joining("\n")));
        System.out.println("-".repeat(50));
    }

    private void processConfigurationOfClass(Map<String, String> configs, Class<? extends AbstractConfigurable> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var object = createObject(clazz);
        List<Field> fields = new ArrayList<>();
        findImportantFields(object.getClass(), fields);
        fillConfigs(object, fields, configs);
    }

    private void fillConfigs(AbstractConfigurable object, List<Field> fields, Map<String, String> configs) throws IllegalAccessException {
        for (Field f : fields) {
            f.setAccessible(true);
            var key = f.getDeclaringClass().getSimpleName() + "::" + f.getName();
            var rawValue = f.get(object);
            var value = getValue(rawValue);
            if (configs.containsKey(key)) {
                Assertions.fail("Found duplicate entry in map: " + key);
            }
            configs.put(key, value);
        }
    }

    private String getValue(Object rawValue) {
        if (rawValue instanceof Integer i)
            return Integer.toString(i);
        if (rawValue instanceof Double d)
            return String.format(Locale.ENGLISH, "%f", d);
        if (rawValue instanceof Boolean b)
            return String.valueOf(b);
        if (rawValue instanceof List<?> s && s.stream().allMatch(it -> it instanceof String))
            return s.stream().map(Object::toString).collect(Collectors.joining(","));

        throw new IllegalArgumentException("RawValue has no type that may be transformed to an Configuration" + rawValue + "[" + rawValue.getClass() + "]");
    }

    private void findImportantFields(Class<?> clazz, List<Field> fields) {
        if (clazz == Object.class || clazz == AbstractConfigurable.class)
            return;

        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Configurable.class))
                fields.add(field);
        }
        findImportantFields(clazz.getSuperclass(), fields);
    }

    private AbstractConfigurable createObject(Class<? extends AbstractConfigurable> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var constructors = Arrays.asList(clazz.getDeclaredConstructors());
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 0)) {
            var constructor = constructors.stream().filter(c -> c.getParameterCount() == 0).findFirst().get();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance();
        }
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Map.class)) {
            var constructor = constructors.stream().filter(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Map.class).findFirst().get();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance(Map.of());
        }

        Assertions.fail("No suitable constructor has been found for " + clazz);
        throw new Error("Not reachable code");
    }

}
