/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;
import io.github.ardoco.textproviderjson.converter.DtoToObjectConverter;
import io.github.ardoco.textproviderjson.converter.JsonConverter;
import io.github.ardoco.textproviderjson.dto.TextDto;
import io.github.ardoco.textproviderjson.error.InvalidJsonException;
import io.github.ardoco.textproviderjson.error.NotConvertableException;

/**
 * This text processor processes texts by sending requests to a microservice, which provides text processing using CoreNLP.
 */
public class TextProcessorService {

    /**
     * processes and annotates a given text by sending requests to a microservice
     * 
     * @param inputText the input text
     * @return the annotated text
     */
    public Text processText(String inputText) throws IOException, InvalidJsonException, NotConvertableException {
        TextDto textDto;
        String jsonText = sendCorenlpRequest(inputText);
        textDto = JsonConverter.fromJsonString(jsonText);
        return new DtoToObjectConverter().convertText(textDto);
    }

    private String sendCorenlpRequest(String inputText) throws IOException {
        inputText = URLEncoder.encode(inputText, StandardCharsets.UTF_8);
        String requestUrl = ConfigManager.getInstance().getMicroserviceUrl() + ConfigManager.getInstance().getCorenlpService() + inputText;
        return sendAuthenticatedGetRequest(requestUrl);
    }

    public String sendAuthenticatedGetRequest(String requestUrl) throws IOException {
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        if (username == null || password == null) {
            throw new IOException("Environment variables USERNAME and PASSWORD must be set.");
        }
        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // Encode the username and password
        String authString = username + ":" + password;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes());
        String authHeaderValue = "Basic " + encodedAuthString;
        con.setRequestProperty("Authorization", authHeaderValue);

        String content = readGetResponse(con);
        con.disconnect();
        return content;
    }

    private String readGetResponse(HttpURLConnection con) throws IOException {
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + con.getResponseCode());
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }
}
