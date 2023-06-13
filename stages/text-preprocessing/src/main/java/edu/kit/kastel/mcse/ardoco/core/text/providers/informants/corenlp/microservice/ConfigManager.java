package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.microservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This Singleton has access to the config file.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Properties properties;

    private ConfigManager(String filePath) {
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(filePath);) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigManager getInstance(String filePath) {
        if (instance == null) {
            instance = new ConfigManager(filePath);
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}