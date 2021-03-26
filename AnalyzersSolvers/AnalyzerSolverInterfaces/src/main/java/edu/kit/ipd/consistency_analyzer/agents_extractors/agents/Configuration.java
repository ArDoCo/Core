package edu.kit.ipd.consistency_analyzer.agents_extractors.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.impl.factory.Lists;

public abstract class Configuration {

    /**
     * Returns the specified property of the config file as a list of strings.
     *
     * @param key name of the specified property
     * @return value of the property as a list of strings
     * @throws Exception if the key is not found in the configuration file.
     */
    protected static List<String> getPropertyAsList(String key, Map<String, String> configs) {
        List<String> values = Lists.mutable.empty();
        String value = configs.get(key);
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
     * Returns the specified property of the config file as a double.
     *
     * @param key name of the specified property
     * @return value of the property as a double
     */
    protected static double getPropertyAsDouble(String key, Map<String, String> configs) {
        return Double.parseDouble(configs.get(key));
    }
}
