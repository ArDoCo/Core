package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
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

public class AbbreviationDisambiguationHelper extends FileBasedCache<ImmutableMap<String, AbbreviationDisambiguationHelper.Abbreviation>> {
    @Configurable
    public static final int limit = 3;
    private static Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private static final String abbreviationsCom = "https://www.abbreviations.com/";
    private static AbbreviationDisambiguationHelper instance;
    private MutableMap<String, Abbreviation> abbreviations;

    public static synchronized @NotNull AbbreviationDisambiguationHelper getInstance() {
        if (instance == null) {
            instance = new AbbreviationDisambiguationHelper();
        }
        return instance;
    }

    private AbbreviationDisambiguationHelper() {
    }

    public ImmutableSet<String> get(@NotNull String abbreviation) {
        //Specifically check. We do not want to mess up our files.
        if (abbreviation == null || abbreviation.isEmpty() || abbreviation.isBlank())
            throw new IllegalArgumentException();

        if (abbreviations == null)
            abbreviations = Maps.mutable.ofMapIterable(load());

        var abbr = abbreviations.computeIfAbsent(abbreviation, this::disambiguate);
        return Sets.immutable.ofAll(abbr.meanings());
    }

    private Abbreviation disambiguate(@NotNull String abbreviation) {
        //Specifically check. We do not want to mess up our files.
        if (abbreviation == null || abbreviation.isEmpty() || abbreviation.isBlank())
            throw new IllegalArgumentException();

        var abbr = crawl(abbreviation);
        abbreviations.put(abbr.abbreviation(), abbr);
        save(abbreviations);

        return abbr;
    }

    public Abbreviation crawl(@NotNull String abbreviation) {
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
     * Gets the abbreviation file from the disk and parses its content.
     *
     * @return the abbreviations
     */
    @Override
    public @NotNull ImmutableMap<String, Abbreviation> load() {
        if (abbreviations != null)
            return Maps.immutable.ofMap(abbreviations);
        try {
            logger.info("Reading abbreviations file");
            var map = toMutableMap(new ObjectMapper().readValue(getFile(), Abbreviation[].class));
            logger.info("Found {} cached abbreviation", map.keySet().size());
            return Maps.immutable.ofMap(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getIdentifier() {
        return "abbreviations";
    }

    @Override
    public ImmutableMap<String, Abbreviation> getDefault() {
        return Maps.immutable.empty();
    }

    @Override
    public void save(ImmutableMap<String, AbbreviationDisambiguationHelper.Abbreviation> content) {
        Collection<Abbreviation> values = content.valuesView().toList();
        try (PrintWriter out = new PrintWriter(getFile())) {
            //Parse before writing to the file, so we don't mess up the entire file due to a parsing error
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(values);
            out.print(json);
            logger.info("Saved abbreviations file");
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        }
    }

    private void save(MutableMap<String, AbbreviationDisambiguationHelper.Abbreviation> content) {
        save(Maps.immutable.ofMap(content));
    }

    private @NotNull MutableMap<String, Abbreviation> toMutableMap(@NotNull Abbreviation[] abbreviations) {
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
