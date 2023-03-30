/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfigurable implements IConfigurable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String CLASS_ATTRIBUTE_CONNECTOR = "::";
    public static final String KEY_VALUE_CONNECTOR = "=";
    public static final String LIST_SEPARATOR = ",";

    private Map<String, String> lastAppliedConfiguration = new HashMap<>();

    protected final <E> List<E> findByClassName(List<String> selected, List<E> instances) {
        List<E> target = new ArrayList<>(0);
        for (var clazz : selected) {
            var elem = instances.stream()
                    .filter(e -> e.getClass().getSimpleName().equals(clazz))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Could not find " + clazz));
            target.add(elem);
        }
        return target;
    }

    @Override
    public final void applyConfiguration(Map<String, String> additionalConfiguration) {
        applyConfiguration(additionalConfiguration, this.getClass());
        delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        this.lastAppliedConfiguration = new HashMap<>(additionalConfiguration);
    }

    @Override
    public Map<String, String> getLastAppliedConfiguration() {
        return Collections.unmodifiableMap(lastAppliedConfiguration);
    }

    protected abstract void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration);

    private void applyConfiguration(Map<String, String> additionalConfiguration, Class<?> currentClass) {
        if (currentClass == Object.class || currentClass == AbstractConfigurable.class)
            return;

        var fields = currentClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Configurable.class)) {
                continue;
            }
            Configurable c = field.getAnnotation(Configurable.class);
            String key = c.key().isBlank() ? (currentClass.getSimpleName() + CLASS_ATTRIBUTE_CONNECTOR + field.getName()) : c.key();
            if (additionalConfiguration.containsKey(key)) {
                setValue(field, additionalConfiguration.get(key));
            }
        }

        applyConfiguration(additionalConfiguration, currentClass.getSuperclass());
    }

    private void setValue(Field field, String value) {
        var clazz = field.getType();
        var parsedValue = parse(field, clazz, value);
        if (parsedValue == null)
            return;

        try {
            field.setAccessible(true);
            field.set(this, parsedValue);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Object parse(Field field, Class<?> fieldsClass, String value) {
        if (fieldsClass == Integer.class || fieldsClass == Integer.TYPE) {
            return Integer.parseInt(value);
        }
        if (fieldsClass == Double.class || fieldsClass == Double.TYPE) {
            return Double.parseDouble(value);
        }
        if (fieldsClass == Boolean.class || fieldsClass == Boolean.TYPE) {
            return Boolean.parseBoolean(value);
        }
        if (fieldsClass.isEnum()) {
            var result = Arrays.stream(fieldsClass.getEnumConstants()).filter(c -> String.valueOf(c).equals(value)).findFirst();
            return result.orElseThrow(() -> new IllegalArgumentException("Unknown Enum Constant " + value));
        }

        if (List.class.isAssignableFrom(fieldsClass) && field.getGenericType() instanceof ParameterizedType parameterizedType) {
            var generics = parameterizedType.getActualTypeArguments();

            if (generics != null && generics.length == 1 && generics[0] == String.class)
                return new ArrayList<>(Arrays.stream(value.split(LIST_SEPARATOR)).toList());
        }

        throw new IllegalArgumentException("Could not find a parse method for fields of type: " + fieldsClass);
    }
}
