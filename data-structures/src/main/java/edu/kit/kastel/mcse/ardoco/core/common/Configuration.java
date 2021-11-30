/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.common;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

/**
 *
 * The base class for configurations of agents and extractors
 *
 */
public abstract class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);

    /**
     * Returns the specified property of the config file as a list of strings.
     *
     * @param key     name of the specified property
     * @param configs the configuration values
     * @return value of the property as a list of strings
     * @throws Exception if the key is not found in the configuration file.
     */
    protected static ImmutableList<String> getPropertyAsList(String key, Map<String, String> configs) {
        MutableList<String> values = Lists.mutable.empty();
        String value = configs.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key: " + key + " not found in config");
        }

        if (value.isBlank()) {
            return values.toImmutable();
        }

        values.addAll(Lists.immutable.with(value.split(" ")).castToCollection());
        values.removeIf(String::isBlank);
        return values.toImmutable();
    }

    /**
     * Returns the specified property of the config file as a double.
     *
     * @param key     name of the specified property
     * @param configs the configuration map
     * @return value of the property as a double
     */
    protected static double getPropertyAsDouble(String key, Map<String, String> configs) {
        return Double.parseDouble(configs.get(key));
    }

    /**
     * Returns the specified property of the config file as a boolean if it is set.
     *
     * @param key     name of the specified property
     * @param configs the configuration map
     * @return value of the property as a boolean. True, if the value for the key is "true", "yes", or "1" ignoring
     *         case.
     */
    protected static boolean isPropertyEnabled(String key, Map<String, String> configs) {
        var propValue = configs.get(key).strip();
        return Boolean.parseBoolean(propValue) || propValue.equalsIgnoreCase("yes") || propValue.equalsIgnoreCase("1");
    }

    /**
     * Get all properties and raw values of a configuration
     *
     * @return all properties with their raw values
     */
    protected abstract Map<String, String> getAllProperties();

    /**
     * Merges a configuration to a configuration map.
     *
     * @param configs the target map
     * @param config  the configuration
     * @throws IllegalArgumentException iff configs are already set
     */
    public static void mergeConfigToMap(Map<String, String> configs, Configuration config) throws IllegalArgumentException {
        for (var c : config.getAllProperties().entrySet()) {
            if (configs.containsKey(c.getKey())) {
                throw new IllegalArgumentException(c.getKey() + " already set");
            }
            configs.put(c.getKey(), c.getValue());
        }
    }

    /**
     * Overrides a configuration in a configuration map (only present values will be overriden).
     *
     * @param configs           the target map
     * @param additionalConfigs the file with additional configs
     */
    public static void overrideConfigInMap(Map<String, String> configs, File additionalConfigs) {
        try (var scan = new Scanner(additionalConfigs, StandardCharsets.UTF_8)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] kv = new String[0];
                if (line == null || line.isBlank()) {
                    logger.warn("Illegal Line in config: \"{}\"", line);
                } else {
                    kv = line.trim().split("=", 2);
                }

                if (kv.length != 2) {
                    logger.warn("Illegal Line in config: \"{}\"", line);
                } else if (configs.containsKey(kv[0].trim())) {
                    configs.put(kv[0].trim(), kv[1].trim());
                }
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
