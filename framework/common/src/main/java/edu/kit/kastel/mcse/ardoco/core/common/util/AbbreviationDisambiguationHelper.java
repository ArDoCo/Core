package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;

public class AbbreviationDisambiguationHelper extends FileBasedCache<ImmutableMap<String, AbbreviationDisambiguationHelper.Abbreviation>> {
    public static final int LIMIT = 2;
    public static final double SIMILARITY_THRESHOLD = 0.9;
    private static Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private static final String abbreviationsCom = "https://www.abbreviations.com/";
    private static final String acronymFinderCom = "https://www.acronymfinder.com/Information-Technology/";
    private static AbbreviationDisambiguationHelper instance;
    private MutableMap<String, Abbreviation> abbreviations;

    public static synchronized @NotNull AbbreviationDisambiguationHelper getInstance() {
        if (instance == null) {
            instance = new AbbreviationDisambiguationHelper();
        }
        return instance;
    }

    private AbbreviationDisambiguationHelper() {
        super("abbreviations", ".json", "");
    }

    public Set<String> disambiguate(@NotNull String abbreviation) {
        //Specifically check. We do not want to mess up our files.
        if (abbreviation == null || abbreviation.isEmpty() || abbreviation.isBlank())
            throw new IllegalArgumentException();

        if (abbreviations == null)
            abbreviations = Maps.mutable.ofMapIterable(load());

        var abbr = abbreviations.computeIfAbsent(abbreviation, this::get);
        return Set.copyOf(abbr.meanings());
    }

    public Set<String> ambiguate(@NotNull String meaning) {
        var set = new HashSet<String>();
        var crossProduct = AbbreviationDisambiguationHelper.getInstance().similarAbbreviations(meaning);
        if (crossProduct.isEmpty())
            return set;
        for (var abbreviationPairs : crossProduct) {
            var text = meaning;
            for (var pair : abbreviationPairs) {
                text = text.replace(pair.second().toLowerCase(), pair.first().toLowerCase());
            }
            set.add(text);
        }
        return set;
    }

    private List<List<Pair<String, String>>> similarAbbreviations(@NotNull String meaning) {
        if (abbreviations == null)
            abbreviations = Maps.mutable.ofMapIterable(load());

        var abbrevs = abbreviations.values();
        var allAbbrevs = new ArrayList<List<Pair<String, String>>>();
        for (var abbr : abbrevs) {
            var listOfPairs = abbr.meanings.stream()
                    .map(m -> new Pair<>(WordSimUtils.getSimilarity(m, meaning), m))
                    .filter(p -> p.first() >= SIMILARITY_THRESHOLD)
                    .map(p -> new Pair<>(abbr.abbreviation, p.second()))
                    .toList();
            if (listOfPairs.isEmpty())
                continue;
            allAbbrevs.add(listOfPairs);
        }
        return Lists.cartesianProduct(allAbbrevs).stream().filter(l -> !l.isEmpty()).toList();
    }

    private Abbreviation get(@NotNull String abbreviation) {
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
        var meanings = crawlAcronymFinderCom(abbreviation);
        meanings.addAll(crawlAbbreviationsCom(abbreviation));
        return new Abbreviation(abbreviation, meanings);
    }

    public LinkedHashSet<String> crawlAbbreviationsCom(@NotNull String abbreviation) {
        var meanings = new LinkedHashSet<String>();
        try {
            Document document = Jsoup.connect(abbreviationsCom + abbreviation).get();
            var elements = document.select("td > p.desc");
            meanings.addAll(elements.stream().limit(LIMIT).map(e -> removeAllBrackets(e.childNode(0).toString())).toList());
            logger.info("Crawler found {} -> {} on {}", abbreviation, meanings, abbreviationsCom);
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Could not parse {} website document, did the layout change?", abbreviationsCom);
        }
        return meanings;
    }

    public LinkedHashSet<String> crawlAcronymFinderCom(@NotNull String abbreviation) {
        var meanings = new LinkedHashSet<String>();
        try {
            Document document = Jsoup.connect(acronymFinderCom + abbreviation + ".html").get();
            var elements = document.select("td.result-list__body__meaning > a, td.result-list__body__meaning");
            meanings.addAll(elements.stream().limit(LIMIT).map(Element::text).map(this::removeAllBrackets).toList());
            logger.info("Crawler found {} -> {} on {}", abbreviation, meanings, acronymFinderCom);
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Could not parse {} website document, did the layout change?", acronymFinderCom);
        }
        return meanings;
    }

    /**
     * Gets the abbreviation file from the disk and parses its content.
     *
     * @return the abbreviations
     */
    @Override
    public @NotNull ImmutableMap<String, Abbreviation> load(boolean allowReload) {
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

    private @NotNull String removeAllBrackets(String text) {
        String prev = null;
        var current = removeBracket(text);
        while (!Objects.equals(prev, current)) {
            prev = current;
            current = removeBracket(current);
        }
        return current.trim().replaceAll("\\s+", " ");
    }

    /**
     * Removes the first bracket and text inbetween, does not support nested brackets
     */
    private @NotNull String removeBracket(String text) {
        var innerMostOpen = text.indexOf("(");
        var innerMostClose = text.indexOf(")");
        if (innerMostOpen == -1 || innerMostClose == -1 || innerMostOpen > innerMostClose)
            return text;

        var start = text.substring(0, innerMostOpen);
        var endBegin = innerMostClose + 1;

        if (endBegin > text.length())
            return start;

        var end = text.substring(endBegin);
        return start + end;
    }

    @JsonSerialize(using = AbbreviationSerializer.class)
    public record Abbreviation(@NotNull String abbreviation, @NotNull LinkedHashSet<String> meanings) {
        @JsonCreator
        public Abbreviation(@JsonProperty("abbreviation") @NotNull String abbreviation, @JsonProperty("meanings") @NotNull String[] meanings) {
            this(abbreviation, new LinkedHashSet<>(Arrays.stream(meanings).toList()));
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
