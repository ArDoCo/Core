package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;

import io.github.ardoco.textproviderjson.converter.JsonConverter;
import io.github.ardoco.textproviderjson.converter.DtoToObjectConverter;
import io.github.ardoco.textproviderjson.dto.TextDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextProcessorService implements TextProcessor {

    private final String microserviceAddress = "localhost:8080";
    private final String requestUrl = "http://" + microserviceAddress + "/stanfordnlp?text=";

    @Override
    public Text processText(String inputText) {
        TextDTO textDto;
        try {
            String jsonText = sendRequest(inputText);
            textDto = JsonConverter.fromJsonString(jsonText);
        } catch (IOException e) {
            return null; // todo error handling
        }
        return new DtoToObjectConverter().convertText(textDto);
    }

    private String sendRequest(String inputText) throws IOException {
        inputText = inputText.replace(" ", "%20");
        URL url = new URL(requestUrl + inputText);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
//        int status = con.getResponseCode();
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
