package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This utility class provides methods to check whether the microservice is available.
 */
public final class MicroserviceChecker {

    private MicroserviceChecker() {
    }

    /**
     * checks if the CoreNLP microservice is available and can provide its services.
     * @return  whether the microservice is available
     */
    public static boolean isMicroserviceAvailable() {
        String requestUrl = ConfigManager.getInstance().getProperty("microserviceUrl") + ConfigManager.getInstance().getProperty("healthService");
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000); // timeout after 5 sec
            int statusCode = con.getResponseCode();
            con.disconnect();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
