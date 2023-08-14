/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;

/**
 * This utility class provides methods to check whether the microservice is available.
 */
public final class MicroserviceChecker {

    private MicroserviceChecker() {
    }

    /**
     * checks if the CoreNLP microservice is available and can provide its services.
     * 
     * @return whether the microservice is available
     */
    public static boolean isMicroserviceAvailable() throws IOException {
        String requestUrl = ConfigManager.getInstance().getProperty("microserviceUrl") + ConfigManager.getInstance().getProperty("healthService");

        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        if (username == null || password == null) {
            throw new IOException("Environment variables USERNAME and PASSWORD must be set.");
        }
        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000); // timeout after 5 sec

        // Encode the username and password
        String authString = username + ":" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
        String authHeaderValue = "Basic " + encodedAuthString;
        con.setRequestProperty("Authorization", authHeaderValue);
        int statusCode = con.getResponseCode();
        con.disconnect();
        return statusCode == HttpURLConnection.HTTP_OK;
    }

}
