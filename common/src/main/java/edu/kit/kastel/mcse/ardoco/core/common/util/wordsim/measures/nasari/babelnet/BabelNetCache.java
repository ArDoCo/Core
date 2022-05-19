/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.nd4j.shade.jackson.core.type.TypeReference;
import org.nd4j.shade.jackson.databind.ObjectMapper;

/**
 * A persistent cache that stores responses from BabelNet queries. Use the {@link #saveToFile()} method to save the
 * current contents of the cache to the file system.
 */
public class BabelNetCache {

    private final Path cacheFilePath;
    private final Map<String, String> queryResponseMap = new HashMap<>();

    /**
     * Constructs a new {@link BabelNetCache} instance. The cache file will be created if it does not exist yet.
     * 
     * @param cacheFilePath the path to the file where the cache is persisted
     * @throws IOException if the cache file could not be read
     */
    public BabelNetCache(Path cacheFilePath) throws IOException {
        this.cacheFilePath = Objects.requireNonNull(cacheFilePath);

        if (!Files.exists(cacheFilePath)) {
            Files.createFile(cacheFilePath);
        }

        String fileContents = Files.readString(cacheFilePath);

        if (!fileContents.isEmpty()) {
            var typeRef = new TypeReference<HashMap<String, String>>() {
            };
            var readMap = new ObjectMapper().readValue(fileContents, typeRef);
            this.queryResponseMap.putAll(readMap);
        }
    }

    /**
     * Checks whether this cache contains a response for the given query.
     * 
     * @param query the query
     * @return Returns {@code true} if this cache has a response for the given query.
     */
    public boolean contains(String query) {
        return this.queryResponseMap.containsKey(Objects.requireNonNull(query));
    }

    /**
     * Attempts to get the appropriate response for the specified query.
     * 
     * @param query the query
     * @return the response, or {@link Optional#empty()} if this cache does not contain a response
     */
    public Optional<String> get(String query) {
        return Optional.ofNullable(this.queryResponseMap.get(Objects.requireNonNull(query)));
    }

    /**
     * Stores the response for the specified query.
     * 
     * @param query        the query
     * @param responseBody the response
     */
    public void insert(String query, String responseBody) {
        Objects.requireNonNull(query);
        Objects.requireNonNull(responseBody);
        this.queryResponseMap.put(query, responseBody);
    }

    /**
     * Saves the contents of this cache in the cache file that was given when the constructor was called.
     * 
     * @throws IOException if an I/O error occurs writing to the cache file
     */
    public void saveToFile() throws IOException {
        saveToFile(this.cacheFilePath);
    }

    /**
     * Saves the contents of this cache to the file at the specified path.
     * 
     * @param targetFile the target file
     * @throws IOException if an I/O error occurs writing to or creating the cache file
     */
    public void saveToFile(Path targetFile) throws IOException {
        var mapper = new ObjectMapper();
        var mapAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.queryResponseMap);
        Files.writeString(targetFile, mapAsString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
