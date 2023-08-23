package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class CustomResponseHandler implements HttpClientResponseHandler<CustomHttpResponse> {
    @Override
    public CustomHttpResponse handleResponse(ClassicHttpResponse response) throws IOException {
        return new CustomHttpResponse(readGetResponse(response), response.getCode());
    }


    private String readGetResponse(ClassicHttpResponse response) throws IOException {
        if (response.getCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + response.getCode());
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
