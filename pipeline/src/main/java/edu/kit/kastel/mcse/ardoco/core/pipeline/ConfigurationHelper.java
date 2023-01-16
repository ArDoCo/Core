/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.AbstractConfigurable;
import edu.kit.kastel.informalin.framework.configuration.Configurable;

public class ConfigurationHelper {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationHelper.class);

    private ConfigurationHelper() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Loads the file that contains additional configurations and returns the Map that consists of the configuration options.
     *
     * @param additionalConfigsFile the file containing the additional configurations
     * @return a Map with the additional configurations
     */
    public static Map<String, String> loadAdditionalConfigs(File additionalConfigsFile) {
        Map<String, String> additionalConfigs = new HashMap<>();
        if (additionalConfigsFile != null && additionalConfigsFile.exists()) {
            try (var scanner = new Scanner(additionalConfigsFile, StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    var line = scanner.nextLine();
                    if (line == null || line.isBlank()) {
                        continue;
                    }
                    var values = line.split(AbstractConfigurable.KEY_VALUE_CONNECTOR, 2);
                    if (values.length != 2) {
                        logger.error(
                                "Found config line \"{}\". Layout has to be: 'KEY" + AbstractConfigurable.KEY_VALUE_CONNECTOR + "VALUE', e.g., 'SimpleClassName" + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + "AttributeName" + AbstractConfigurable.KEY_VALUE_CONNECTOR + "42",
                                line);
                    } else {
                        additionalConfigs.put(values[0], values[1]);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return additionalConfigs;
    }

    public static Map<String, String> getDefaultConfigurationOptions() {
        Map<String, String> configs = new TreeMap<>();
        var reflectAccess = new Reflections("edu.kit.kastel.mcse.ardoco");
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> c.getPackageName().startsWith("edu.kit.kastel.mcse.ardoco"))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();
        for (var clazz : classesThatMayBeConfigured) {
            try {
                logger.debug("Loading class {}", clazz.getSimpleName());
                processConfigurationOfClass(configs, clazz);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return configs;
    }

    protected static void processConfigurationOfClass(Map<String, String> configs, Class<? extends AbstractConfigurable> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var object = createObject(clazz);
        List<Field> fields = new ArrayList<>();
        findImportantFields(object.getClass(), fields);
        fillConfigs(object, fields, configs);
    }

    private static void fillConfigs(AbstractConfigurable object, List<Field> fields, Map<String, String> configs) throws IllegalAccessException {
        for (Field f : fields) {
            f.setAccessible(true);
            var key = f.getDeclaringClass().getSimpleName() + AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR + f.getName();
            var rawValue = f.get(object);
            var value = getValue(object, f, rawValue);
            if (configs.containsKey(key)) {
                throw new IllegalArgumentException("Found duplicate entry in map: " + key);
            }
            configs.put(key, value);
        }
    }

    private static String getValue(AbstractConfigurable parent, Field field, Object rawValue) {
        if (rawValue instanceof Integer i) {
            return Integer.toString(i);
        }
        if (rawValue instanceof Double d) {
            return String.format(Locale.ENGLISH, "%f", d);
        }
        if (rawValue instanceof Boolean b) {
            return String.valueOf(b);
        }
        if (rawValue instanceof List<?> s && s.stream().allMatch(String.class::isInstance)) {
            return s.stream().map(Object::toString).collect(Collectors.joining(AbstractConfigurable.LIST_SEPARATOR));
        }
        if (rawValue instanceof Enum<?> e) {
            return e.name();
        }

        Class<?> cls = rawValue == null ? null : rawValue.getClass();
        throw new IllegalArgumentException(
                "RawValue has no type that may be transformed to an Configuration " + rawValue + "[" + cls + "] .. Affected field: " + field
                        .getName() + "@" + parent.getClass().getSimpleName());

    }

    private static void findImportantFields(Class<?> clazz, List<Field> fields) {
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

    private static AbstractConfigurable createObject(Class<? extends AbstractConfigurable> clazz) throws InvocationTargetException, InstantiationException,
            IllegalAccessException {
        var constructors = Arrays.asList(clazz.getDeclaredConstructors());
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 0)) {
            var constructor = constructors.stream().filter(c -> c.getParameterCount() == 0).findFirst().orElseThrow();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance();
        }
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Map.class)) {
            var constructor = constructors.stream().filter(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Map.class).findFirst().orElseThrow();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance(Map.of());
        }
        if (constructors.stream().anyMatch(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == DataRepository.class)) {
            var constructor = constructors.stream()
                    .filter(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == DataRepository.class)
                    .findFirst()
                    .orElseThrow();
            constructor.setAccessible(true);
            return (AbstractConfigurable) constructor.newInstance(new Object[1]);
        }
        throw new IllegalStateException("Not reachable code reached for " + clazz.getSimpleName());
    }

}
