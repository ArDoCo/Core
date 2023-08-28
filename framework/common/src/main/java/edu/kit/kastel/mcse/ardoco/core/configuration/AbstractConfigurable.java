/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public abstract class AbstractConfigurable implements IConfigurable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String CLASS_ATTRIBUTE_CONNECTOR = "::";
    public static final String KEY_VALUE_CONNECTOR = "=";
    public static final String LIST_SEPARATOR = ",";

    private SortedMap<String, String> lastAppliedConfiguration = new TreeMap<>();

    @Override
    public final void applyConfiguration(SortedMap<String, String> additionalConfiguration) {
        applyConfiguration(additionalConfiguration, this, this.getClass());
        delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        this.lastAppliedConfiguration = new TreeMap<>(additionalConfiguration);
    }

    @Override
    public SortedMap<String, String> getLastAppliedConfiguration() {
        return Collections.unmodifiableSortedMap(lastAppliedConfiguration);
    }

    protected abstract void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration);

    private void applyConfiguration(SortedMap<String, String> additionalConfiguration, AbstractConfigurable configurable, Class<?> currentClassInHierarchy) {
        if (currentClassInHierarchy == Object.class || currentClassInHierarchy == AbstractConfigurable.class)
            return;

        if (currentClassInHierarchy.getAnnotation(NoConfiguration.class) != null) {
            logger.debug("Skipping configuration for class {}", currentClassInHierarchy.getSimpleName());
            return;
        }

        var fields = currentClassInHierarchy.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Configurable.class)) {
                continue;
            }
            String key = getKeyOfField(configurable, currentClassInHierarchy, field);
            if (additionalConfiguration.containsKey(key)) {
                setValue(field, additionalConfiguration.get(key));
            }
        }

        applyConfiguration(additionalConfiguration, configurable, currentClassInHierarchy.getSuperclass());
    }

    /**
     * Returns the key (for the configuration file) of a field. If the field is marked as ChildClassConfigurable, the key is based on the class of the
     * configurable object. Otherwise, the key is based on the class where the field is defined.
     * 
     * @param configurable            the configurable object
     * @param currentClassInHierarchy the class where the field is defined
     * @param field                   the field
     * @return the key of the field
     */
    public static String getKeyOfField(AbstractConfigurable configurable, Class<?> currentClassInHierarchy, Field field) {
        Configurable configurableAnnotation = field.getAnnotation(Configurable.class);
        ChildClassConfigurable childClassConfigurableAnnotation = field.getAnnotation(ChildClassConfigurable.class);

        if (childClassConfigurableAnnotation != null && !configurableAnnotation.key().isBlank()) {
            throw new IllegalStateException("You cannot define a key for a field that is marked as ChildClassConfigurable.");
        }

        String classOfDefinition = childClassConfigurableAnnotation == null ? currentClassInHierarchy.getSimpleName() : configurable.getClass().getSimpleName();

        return configurableAnnotation.key().isBlank() ? (classOfDefinition + CLASS_ATTRIBUTE_CONNECTOR + field.getName()) : configurableAnnotation.key();
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
