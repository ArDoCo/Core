package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet;

import org.nd4j.shade.jackson.core.type.TypeReference;
import org.nd4j.shade.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BabelNetCache {

    private final Path cacheFilePath;
    private final Map<String, String> queryResponseMap = new HashMap<>();

    public BabelNetCache(Path cacheFilePath) throws IOException {
        this.cacheFilePath = cacheFilePath;

        if (!Files.exists(cacheFilePath)) {
            Files.createFile(cacheFilePath);
        }

        String fileContents = Files.readString(cacheFilePath);

        if (!fileContents.isEmpty()) {
            var typeRef = new TypeReference<HashMap<String, String>>() {};
            var readMap = new ObjectMapper().readValue(fileContents, typeRef);
            this.queryResponseMap.putAll(readMap);
        }
    }

    public boolean contains(String query) {
        return queryResponseMap.containsKey(query);
    }

    public Optional<String> get(String query) {
        return Optional.ofNullable(this.queryResponseMap.get(query));
    }

    public void insert(String query, String responseBody) {
        this.queryResponseMap.put(query, responseBody);
    }

    public void saveToFile() throws IOException {
        saveToFile(this.cacheFilePath);
    }

    public void saveToFile(Path targetFile) throws IOException {
        var mapper = new ObjectMapper();
        var mapAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.queryResponseMap);
        Files.writeString(targetFile, mapAsString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
