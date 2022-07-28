/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetInvalidKeyException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetRequestLimitException;

/**
 * Provides BabelNet related data.
 */
public class BabelNetDataSource {

    private static final Gson GSON = new Gson();

    private final BabelNetCache cache;
    private final BabelNetHttpApi httpApi;

    /**
     * Constructs a new {@link BabelNetDataSource} instance.
     * 
     * @param apiKey        the api key necessary to communicate with the official BabelNet API
     * @param cacheFilePath the path to the file that caches API responses
     * @throws IOException if the cache file could not be read
     */
    public BabelNetDataSource(String apiKey, Path cacheFilePath) throws IOException {
        this.cache = new BabelNetCache(cacheFilePath);
        this.httpApi = new BabelNetHttpApi(apiKey);
    }

    /**
     * Attempts to retrieve the ids of synsets that are related to the given lemma.
     * 
     * @param lemma the lemma
     * @return the list of synset ids
     * @throws IOException          if an I/O error occurs while communicating with the BabelNet API or the cache
     * @throws InterruptedException if the communication with the BabelNet HTTP API is interrupted
     */
    public List<BabelNetSynsetId> getSensesOfLemma(String lemma)
            throws IOException, InterruptedException, BabelNetInvalidKeyException, BabelNetRequestLimitException {

        Objects.requireNonNull(lemma);

        String response = this.cache.get(lemma).orElse(null);

        if (response == null) {
            response = this.httpApi.querySynsetIdsOfLemma(lemma);

            this.cache.insert(lemma, response);
            this.cache.saveToFile();
        }

        var list = new ArrayList<BabelNetSynsetId>();

        for (JsonElement jsonElement : GSON.fromJson(response, JsonArray.class)) {
            var id = new BabelNetSynsetId(jsonElement.getAsJsonObject().get("id").getAsString());
            list.add(id);
        }

        return list;
    }

}
