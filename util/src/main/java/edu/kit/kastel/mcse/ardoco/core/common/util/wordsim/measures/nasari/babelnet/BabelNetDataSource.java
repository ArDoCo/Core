package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BabelNetDataSource {

    private final BabelNetCache cache;
    private final BabelNetHttpApi httpApi;
    @Nullable private final Path cacheFilePath;

    public BabelNetDataSource(String apiKey) throws IOException {
        this.cache = new BabelNetCache(null); // TODO
        this.httpApi = new BabelNetHttpApi(apiKey);
        this.cacheFilePath = null;
    }

    public BabelNetDataSource(String apiKey, @Nullable Path cacheFilePath) throws IOException {
        this.cache = cacheFilePath == null ? new BabelNetCache() : new BabelNetCache(cacheFilePath);
        this.httpApi = new BabelNetHttpApi(apiKey);
        this.cacheFilePath = cacheFilePath;
    }

    // TODO: Code Cleanup

    public List<BabelNetSynsetId> getSensesOfLemma(String lemma) throws IOException, InterruptedException {
        String response = this.cache.get(lemma).orElse(null);

        if (response == null) {
            response = this.httpApi.querySynsetIdsOfLemma(lemma);
            this.cache.insert(lemma, response);

            if (this.cacheFilePath != null) {
                this.cache.saveToFile(this.cacheFilePath);
            }
        }

        // Parse response
        var list = new ArrayList<BabelNetSynsetId>();
        var gson = new Gson();
        var array = gson.fromJson(response, JsonArray.class);

        for (JsonElement jsonElement : array) {
            var synsetDescriptor = jsonElement.getAsJsonObject();
            var synsetIdStr = synsetDescriptor.get("id").getAsString();
            list.add(new BabelNetSynsetId(synsetIdStr));
        }

        return list;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var dataSource = new BabelNetDataSource(System.getenv("babelnet_key"), Path.of("babelnet_cache.json"));

        var senses = dataSource.getSensesOfLemma("hood");

        for (BabelNetSynsetId sense : senses) {
            System.out.println("- " + sense);
        }
    }

}
