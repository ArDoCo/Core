package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.factory.Lists;

public abstract class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);

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
        try (Scanner scan = new Scanner(additionalConfigs)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line == null || line.isBlank()) {
                    logger.warn("Illegal Line in config: \"" + line + "\"");
                    continue;
                }

                String[] kv = line.trim().split("=", 2);
                if (kv.length != 2) {
                    logger.warn("Illegal Line in config: \"" + line + "\"");
                    continue;
                }

                if (configs.containsKey(kv[0].trim())) {
                    configs.put(kv[0].trim(), kv[1].trim());
                }
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
