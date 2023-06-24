package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;

import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;
import io.github.ardoco.textproviderjson.converter.JsonConverter;
import io.github.ardoco.textproviderjson.converter.DtoToObjectConverter;
import io.github.ardoco.textproviderjson.dto.TextDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This text processor processes texts by sending requests to a microservice, which provides text processing using CoreNLP.
 */
public class TextProcessorService {

    /**
     * processes and annotates a given text by sending requests to a microservice
     * @param inputText the input text
     * @return          the annotated text
     */
    public Text processText(String inputText) throws IOException {
        TextDTO textDto;
        String jsonText = sendCorenlpRequest(inputText);
        textDto = JsonConverter.fromJsonString(jsonText);
        return new DtoToObjectConverter().convertText(textDto);
    }

    private String sendCorenlpRequest(String inputText) throws IOException {
        inputText = URLEncoder.encode(inputText, StandardCharsets.UTF_8);
        String requestUrl = ConfigManager.getInstance().getProperty("microserviceUrl")
                + ConfigManager.getInstance().getProperty("corenlpService")
                + inputText;
        return sendGetRequest(requestUrl);
    }

    private String sendGetRequest(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + con.getResponseCode());
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream())
        );
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }
}
