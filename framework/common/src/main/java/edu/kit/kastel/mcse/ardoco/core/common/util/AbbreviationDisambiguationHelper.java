package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class AbbreviationDisambiguationHelper {
    private static Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private static final String abbreviationsCom = "https://www.abbreviations.com/";
    private static MutableMap<String, Abbreviation> abbreviations;
    private static File abbreviationsFile;
    @Configurable
    public static final int limit = 3;

    private AbbreviationDisambiguationHelper() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    public static ImmutableSet<String> get(@NotNull String abbreviation) {
        //Specifically check. We do not want to mess up our files.
        if (abbreviation == null || abbreviation.isEmpty() || abbreviation.isBlank())
            throw new IllegalArgumentException();

        if (abbreviations == null)
            abbreviations = read();

        var abbr = abbreviations.computeIfAbsent(abbreviation, AbbreviationDisambiguationHelper::disambiguate);
        return Sets.immutable.ofAll(abbr.meanings());
    }

    private static Abbreviation disambiguate(@NotNull String abbreviation) {
        //Specifically check. We do not want to mess up our files.
        if (abbreviation == null || abbreviation.isEmpty() || abbreviation.isBlank())
            throw new IllegalArgumentException();

        var abbr = crawl(abbreviation);
        abbreviations.put(abbr.abbreviation(), abbr);
        save();

        return abbr;
    }

    public static Abbreviation crawl(@NotNull String abbreviation) {
        logger.info("Using crawler to disambiguate {}", abbreviation);
        MutableSet<String> meanings = Sets.mutable.empty();
        try {
            Document document = Jsoup.connect(abbreviationsCom + abbreviation).get();
            var elements = document.select("td > p.desc");
            meanings.addAll(elements.stream().limit(3).map(e -> e.childNode(0).toString()).toList());
            logger.info("Crawler found {} -> {}", abbreviation, meanings);
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        } catch (IndexOutOfBoundsException e) {
            logger.error("Could not parse abbreviations.com website document, did the layout change?");
        }
        return new Abbreviation(abbreviation, meanings);
    }

    /**
     * Gets the abbreviation file from the disk. Creates a new file, if the file doesn't exist. Can throw an IOException during file creation.
     *
     * @return the file
     * @throws IOException can be caused when creating a new file
     */
    private static @NotNull File getAbbreviationsFile() throws IOException {
        if (abbreviationsFile != null)
            return abbreviationsFile;

        AppDirs appDirs = AppDirsFactory.getInstance();
        var arDoCoDataDir = appDirs.getUserDataDir("ArDoCo", null, "MCSE", true);
        //var projectDataDir = arDoCoDataDir + "/projects/" + DataRepositoryHelper.getProjectPipelineData(dataRepository).getProjectName();
        abbreviationsFile = new File(arDoCoDataDir + "/abbreviations.json");
        if (abbreviationsFile.getParentFile().mkdirs()) {
            logger.info("Created directory {}", abbreviationsFile.getParentFile().getCanonicalPath());
        }
        if (abbreviationsFile.createNewFile()) {
            logger.info("Created abbreviation file {}", abbreviationsFile.getCanonicalPath());
            save();
        }

        return abbreviationsFile;
    }

    /**
     * Gets the abbreviation file from the disk and parses its content.
     *
     * @return the abbreviations
     */
    public static @NotNull MutableMap<String, Abbreviation> read() {
        try {
            logger.info("Reading abbreviations file");
            var map = toMutableMap(new ObjectMapper().readValue(getAbbreviationsFile(), Abbreviation[].class));
            logger.info("Found {} cached abbreviation", map.keySet().size());
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void save() {
        Collection<Abbreviation> values = Sets.mutable.empty();
        if (abbreviations != null) {
            values = abbreviations.values();
        }
        try (PrintWriter out = new PrintWriter(getAbbreviationsFile())) {
            //Parse before writing to the file, so we don't mess up the entire file due to a parsing error
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(values);
            out.print(json);
            logger.info("Saved abbreviations file");
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        }
    }

    private static @NotNull MutableMap<String, Abbreviation> toMutableMap(@NotNull Abbreviation[] abbreviations) {
        var all = new HashMap<String, Abbreviation>();
        for (var abbr : abbreviations) {
            all.put(abbr.abbreviation(), abbr);
        }
        return Maps.mutable.ofMap(all);
    }

    @JsonSerialize(using = AbbreviationSerializer.class)
    public record Abbreviation(@NotNull String abbreviation, @NotNull MutableSet<String> meanings) {
        @JsonCreator
        public Abbreviation(@JsonProperty("abbreviation") @NotNull String abbreviation, @JsonProperty("meanings") @NotNull String[] meanings) {
            this(abbreviation, Sets.mutable.ofAll(Arrays.stream(meanings).toList()));
        }
    }

    private static class AbbreviationSerializer extends JsonSerializer<Abbreviation> {

        @Override
        public void serialize(Abbreviation abbreviation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("abbreviation", abbreviation.abbreviation());
            jsonGenerator.writeArrayFieldStart("meanings");
            var meanings = abbreviation.meanings();
            for (var meaning : meanings) {
                jsonGenerator.writeString(meaning);
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
    }
}
