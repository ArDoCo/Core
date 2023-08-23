/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    private String sendAuthenticatedGetRequest(String requestUrl) throws IOException {
        HttpCommunicator httpCommunicator = new HttpCommunicator();
        CustomHttpResponse response = httpCommunicator.sendAuthenticatedGetRequest(requestUrl);
        return response.responseBody();
    }
}
