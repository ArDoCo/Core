package edu.kit.kastel.mcse.ardoco.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

public class SystemParameters {

    private final Properties prop = new Properties();
    private static final Logger LOGGER = LogManager.getLogger(SystemParameters.class);

    public SystemParameters(String filepath, boolean isResource) {

        if (isResource) {

            Object loader = new Object() {
            };

            try (InputStream inputStream = loader.getClass().getResourceAsStream(filepath)) {
                prop.load(inputStream);
            } catch (IOException e) {
                LOGGER.debug(e.getMessage(), e.getCause());
            }
        } else {
            try (InputStream inputStream = new FileInputStream(filepath)) {
                prop.load(inputStream);
            } catch (IOException e) {
                LOGGER.debug(e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * Returns the specified property of the config file as a string.
     *
     * @param key name of the specified property
     * @return value of the property as a string
     */
    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    /**
     * Returns the specified property of the config file as a double.
     *
     * @param key name of the specified property
     * @return value of the property as a double
     */
    public double getPropertyAsDouble(String key) {
        try {
            return Double.parseDouble(prop.getProperty(key));
        } catch (NumberFormatException n) {
            LOGGER.debug(n.getMessage(), n.getCause());
            return -1;
        }
    }

    /**
     * Returns the specified property of the config file as an int.
     *
     * @param key name of the specified property
     * @return value of the property as an int
     */
    public int getPropertyAsInt(String key) {
        try {
            return Integer.parseInt(prop.getProperty(key));
        } catch (NumberFormatException n) {
            LOGGER.debug(n.getMessage(), n.getCause());
            return -1;
        }
    }

    /**
     * Returns the specified property of the config file as a list of strings.
     *
     * @param key name of the specified property
     * @return value of the property as a list of strings
     * @throws Exception if the key is not found in the configuration file.
     */
    public List<String> getPropertyAsList(String key) {
        List<String> values = Lists.mutable.empty();
        String value = prop.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Key: " + key + " not found in config");
        }

        if (value.strip().length() == 0) {
            return values;
        }

        values.addAll(List.of(value.split(" ")));
        values.removeIf(String::isBlank);
        return values;
    }

    /**
     * Identifies the meant specific enum of the class clazz by the data string and returns it.
     *
     * @param <T>   the enum identified by the string
     * @param data  the string that identifies the enum, the names have to be equal
     * @param clazz the class that holds the enum
     * @return the enum identified by the given string
     */
    public <T extends Enum<T>> ImmutableList<T> getPropertyAsListOfEnumTypes(String data, Class<T> clazz) {
        MutableList<T> selectedValues = Lists.mutable.empty();
        T[] values = clazz.getEnumConstants();
        List<String> valueList = getPropertyAsList(data);

        for (T val : values) {
            if (valueList.contains(val.name())) {
                selectedValues.add(val);
            }
        }

        return selectedValues.toImmutable();

    }

}
