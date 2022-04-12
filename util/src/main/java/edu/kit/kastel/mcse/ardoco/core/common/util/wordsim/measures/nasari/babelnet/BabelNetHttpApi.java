package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BabelNetHttpApi {

    private static final String LANGUAGE = "EN";
    private static final String WORD_SENSE_QUERY = "https://babelnet.io/v6/getSynsetIds?lemma={lemma}&searchLang={searchLang}&key={key}";

    private final String apiKey;

    public BabelNetHttpApi(String apiKey) {
        this.apiKey = apiKey;
    }

    public String querySynsetIdsOfLemma(String lemma) throws IOException, InterruptedException {
        String url = WORD_SENSE_QUERY
                .replace("{lemma}", lemma)
                .replace("{searchLang}", LANGUAGE)
                .replace("{key}", this.apiKey);

        var httpClient = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        return response.body();
    }

}
