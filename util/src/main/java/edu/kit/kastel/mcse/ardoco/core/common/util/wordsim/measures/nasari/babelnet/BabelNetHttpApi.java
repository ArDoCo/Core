package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Allows interacting with the official BabelNet HTTP API.
 */
public class BabelNetHttpApi {

    private static final String LANGUAGE = "EN";
    private static final String WORD_SENSE_QUERY = "https://babelnet.io/v6/getSynsetIds?lemma={lemma}&searchLang={searchLang}&key={key}";

    private final String apiKey;

	/**
	 * Constructs a new instance of {@link BabelNetHttpApi}.
	 * @param apiKey the api key necessary to interact with BabelNet
	 */
    public BabelNetHttpApi(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey);
    }

	/**
	 * Queries the BabelNet API for ids of synsets that are related to the given lemma.
	 * @param lemma the lemma
	 * @return the response body of this query
	 * @throws IOException if an I/O error occurs when sending or receiving
	 * @throws InterruptedException if the operation is interrupted
	 */
    public String querySynsetIdsOfLemma(String lemma) throws IOException, InterruptedException {
        // Return type maybe should already be List<BabelNetSynsetId>

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
