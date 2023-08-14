/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Singleton manages access to the config file.
 */
public class ConfigManager {

    Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private static ConfigManager instance;
    private final Properties properties;
    private static final String FILE_PATH = "config.properties";

    private ConfigManager() {
        properties = new Properties();
        try (InputStream fileInputStream = ConfigManager.class.getClassLoader().getResourceAsStream(FILE_PATH);) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            logger.warn("Could not load config file. ", e);
            properties.setProperty("microserviceUrl", "http://localhost:8080");
            properties.setProperty("nlpProviderSource", "local");
            properties.setProperty("corenlpService", "/stanfordnlp?text=");
            properties.setProperty("healthService", "/stanfordnlp/health");
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
