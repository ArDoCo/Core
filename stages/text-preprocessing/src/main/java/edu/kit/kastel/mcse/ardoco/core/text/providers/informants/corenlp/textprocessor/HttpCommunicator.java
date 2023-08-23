package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class HttpCommunicator {

    /**
     * The returned CloseableHttpResponse must be closed after usage.
     * @param requestUrl    The URL to send the GET request to.
     * @return              The response of the GET request.
     * @throws IOException   If the request fails.
     */
    public CloseableHttpResponse sendAuthenticatedGetRequest(String requestUrl) throws IOException {
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        if (username == null || password == null) {
            throw new IOException("Environment variables USERNAME and PASSWORD must be set.");
        }

        HttpGet request = new HttpGet(requestUrl);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        return httpClient.execute(request);
    }

    public String readGetResponse(CloseableHttpResponse response) throws IOException {
        if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + response.getStatusLine().getStatusCode());
        }
        HttpEntity entity = response.getEntity();
        BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }
}
