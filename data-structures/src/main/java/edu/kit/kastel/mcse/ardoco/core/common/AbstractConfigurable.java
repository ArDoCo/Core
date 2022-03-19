/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractAgent;

public abstract class AbstractConfigurable implements IConfigurable {
    protected static final String LIST_SEPARATOR = ",";

    protected final Logger logger = LogManager.getLogger(this.getClass());

    protected final <E> List<E> findByClassName(List<String> selected, List<E> instances) {
        MutableList<E> target = Lists.mutable.empty();
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
    }

    protected abstract void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration);

    private void applyConfiguration(Map<String, String> additionalConfiguration, Class<?> currentClass) {
        if (currentClass == Object.class || currentClass == AbstractAgent.class)
            return;

        var fields = currentClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Configurable.class)) {
                continue;
            }
            Configurable c = field.getAnnotation(Configurable.class);
            String key = c.key().isBlank() ? (currentClass.getSimpleName() + "::" + field.getName()) : c.key();
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
        if (List.class.isAssignableFrom(fieldsClass) && field.getGenericType()instanceof ParameterizedType parameterizedType) {
            var generics = parameterizedType.getActualTypeArguments();

            if (generics != null && generics.length == 1 && generics[0] == String.class)
                return new ArrayList<>(Arrays.stream(value.split(LIST_SEPARATOR)).toList());
        }

        throw new IllegalArgumentException("Could not find a parse method for fields of type: " + fieldsClass);
    }
}
