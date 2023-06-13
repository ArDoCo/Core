package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This Singleton manages access to the config file.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private final Properties properties;
    private final String filePath = "src/main/java/edu/kit/kastel/mcse/ardoco/core/text/providers/informants/corenlp/config/config.properties";

    private ConfigManager() {
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(filePath);) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * gets the value of the given key in the config file
     * @param key   the key
     * @return      the value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * sets the value of the given key in the config file
     * @param key       the key
     * @param value     the new value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}