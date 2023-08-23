package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import java.io.IOException;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

public class HttpCommunicator {


    public CustomHttpResponse sendAuthenticatedGetRequest(String requestUrl) throws IOException {
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        if (username == null || password == null) {
            throw new IOException("Environment variables USERNAME and PASSWORD must be set.");
        }

        HttpGet request = new HttpGet(requestUrl);
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(username, password.toCharArray()));
        HttpClientResponseHandler<CustomHttpResponse> responseHandler = new CustomResponseHandler();
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build()) {
            return httpClient.execute(request, responseHandler);
        }
    }
}
