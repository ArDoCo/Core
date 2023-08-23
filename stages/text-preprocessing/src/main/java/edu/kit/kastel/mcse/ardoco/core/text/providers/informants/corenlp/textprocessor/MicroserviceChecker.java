/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import java.io.IOException;
import java.net.HttpURLConnection;

import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;

import org.apache.http.client.methods.CloseableHttpResponse;

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
        String requestUrl = ConfigManager.getInstance().getMicroserviceUrl() + ConfigManager.getInstance().getHealthService();
        CloseableHttpResponse response = new HttpCommunicator().sendAuthenticatedGetRequest(requestUrl);
        response.close();
        return response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK;
    }

}
