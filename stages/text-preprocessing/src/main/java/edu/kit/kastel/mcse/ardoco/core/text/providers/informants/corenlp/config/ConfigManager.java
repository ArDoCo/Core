/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This Singleton manages access to the config file.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private final Properties properties;
    private static final String filePath = "config.properties";

    private ConfigManager() {
        properties = new Properties();
        try (InputStream fileInputStream = ConfigManager.class.getClassLoader().getResourceAsStream(filePath);) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (System.getenv("MICROSERVICE_URL") != null) {
            properties.setProperty("microserviceUrl", System.getenv("MICROSERVICE_URL"));
        }
        if (System.getenv("NLP_PROVIDER_SOURCE") != null) {
            properties.setProperty("nlpProviderSource", System.getenv("NLP_PROVIDER_SOURCE"));
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
     * 
     * @param key the key
     * @return the value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * sets the value of the given key in the config file
     * 
     * @param key   the key
     * @param value the new value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
