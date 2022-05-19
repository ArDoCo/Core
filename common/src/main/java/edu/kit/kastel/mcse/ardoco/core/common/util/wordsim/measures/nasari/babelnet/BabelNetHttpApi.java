package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetInvalidKeyException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetRequestLimitException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

/**
 * Allows interacting with the official BabelNet HTTP API.
 */
public class BabelNetHttpApi {

	// Sadly, there is no official documentation from BabelNet. These error messages have to suffice. Must be lowercase!
	private static final String INVALID_KEY_ERROR = "your key is not valid";
	private static final String REQUEST_LIMIT_REACHED_ERROR = "the daily requests limit has been reached";
	private static final Duration FIRST_ATTEMPT_TIMEOUT = Duration.ofSeconds(2);

	private static final String LANGUAGE = "EN";
	private static final String WORD_SENSE_QUERY = "https://babelnet.io/v6/getSynsetIds" +
		"?lemma={lemma}" +
		"&searchLang={searchLang}" +
		"&targetLang={targetLang}" +
		"&key={key}";

	// ^ This query just fetches the ids of the found synsets.
	// BabelNet allows another query that directly fetches all information about the found synsets.
	// This may be useful in the future do discard synsets that are irrelevant for the current context.

	private final String apiKey;

	/**
	 * Constructs a new instance of {@link BabelNetHttpApi}.
	 *
	 * @param apiKey the api key necessary to interact with BabelNet
	 */
	public BabelNetHttpApi(String apiKey) {
		this.apiKey = Objects.requireNonNull(apiKey);
	}

	/**
	 * Queries the BabelNet API for ids of synsets that are related to the given lemma.
	 *
	 * @param lemma the lemma
	 * @return the response body of this query
	 * @throws IOException          if an I/O error occurs when sending or receiving
	 * @throws InterruptedException if the operation is interrupted
	 */
	public String querySynsetIdsOfLemma(String lemma) throws IOException, InterruptedException, BabelNetRequestLimitException, BabelNetInvalidKeyException {
		lemma = URLEncoder.encode(lemma, StandardCharsets.UTF_8);

		String url = WORD_SENSE_QUERY
			.replace("{lemma}", lemma)
			.replace("{searchLang}", LANGUAGE)
			.replace("{targetLang}", LANGUAGE)
			.replace("{key}", this.apiKey);

		var request = HttpRequest.newBuilder()
			.GET()
			.uri(URI.create(url))
			.build();

		try {
			return sendGetRequest(request);
		}
		catch (IOException | InterruptedException e) {
			// Exception could be caused by too many queries in a short timespan
			// => wait a bit and try again
			Thread.sleep(FIRST_ATTEMPT_TIMEOUT.toMillis());
			return sendGetRequest(request);
		}
	}

	private String sendGetRequest(HttpRequest request) throws IOException, InterruptedException,
		BabelNetInvalidKeyException, BabelNetRequestLimitException {

		HttpClient client = HttpClient.newHttpClient();

		var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		String body = response.body();

		// Do some basic error checking with known error messages
		if (body.toLowerCase(Locale.ROOT).contains(INVALID_KEY_ERROR)) {
			throw new BabelNetInvalidKeyException(body);
		}
		else if (body.toLowerCase(Locale.ROOT).contains(REQUEST_LIMIT_REACHED_ERROR)) {
			throw new BabelNetRequestLimitException(body);
		}

		return body;
	}

}
