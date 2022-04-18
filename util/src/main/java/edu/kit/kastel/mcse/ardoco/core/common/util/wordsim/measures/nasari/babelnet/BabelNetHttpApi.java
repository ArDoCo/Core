package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetInvalidKeyException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetRequestLimitException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

/**
 * Allows interacting with the official BabelNet HTTP API.
 */
public class BabelNetHttpApi {

    // TODO: Explicitly specify result language

    // Sadly, there is no official documentation from BabelNet. So these error messages have to suffice. Must be lowercase!
    private static final String INVALID_KEY_ERROR = "your key is not valid";
    private static final String REQUEST_LIMIT_REACHED_ERROR = "the daily requests limit has been reached";

    private static final String LANGUAGE = "EN";
    private static final String WORD_SENSE_QUERY = "https://babelnet.io/v6/getSynsetIds?lemma={lemma}&searchLang={searchLang}&key={key}";

    private final String apiKey;
    private long lastQuery = System.currentTimeMillis();
    private long start = System.currentTimeMillis();

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
    public String querySynsetIdsOfLemma(String lemma) throws IOException, InterruptedException, BabelNetRequestLimitException, BabelNetInvalidKeyException {
        if (System.currentTimeMillis() - lastQuery < 2000) {
            Thread.sleep(2000);
        }

        lastQuery = System.currentTimeMillis();

        lemma = URLEncoder.encode(lemma, StandardCharsets.UTF_8);

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

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            var body = response.body();

            // Do some basic error checking with known error messages
            if (body.toLowerCase(Locale.ROOT).contains(INVALID_KEY_ERROR)) {
                throw new BabelNetInvalidKeyException(body);
            } else if (body.toLowerCase(Locale.ROOT).contains(REQUEST_LIMIT_REACHED_ERROR)) {
                throw new BabelNetRequestLimitException(body);
            }

            lastQuery = System.currentTimeMillis();

            System.out.println("[" + (System.currentTimeMillis() - start) + "] Worked: " + url); // TODO: REMOVE

            return body;
        }
        catch (IOException | BabelNetException e) {
            System.err.println("[" + (System.currentTimeMillis() - start) + "] Failed: " + url); // TODO: REMOVE & CLEANUP
            e.printStackTrace();
            throw e;
        }
    }

}
