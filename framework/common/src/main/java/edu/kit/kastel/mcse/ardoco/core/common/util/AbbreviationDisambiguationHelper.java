package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;

public final class AbbreviationDisambiguationHelper extends FileBasedCache<MutableMap<String, Disambiguation>> {
    /**
     * Matches abbreviations with up to 1 lowercase letter between uppercase letters. Accounts for camelCase by lookahead, e.g. UserDBAdapter is matched as "DB"
     * rather than "DBA". Matches abbreviations at any point in the word, including at the start and end.
     */
    private final static Pattern abbreviationsPattern = Pattern.compile("(?:([A-Z]+[a-z]?)+[A-Z])" + "(?=([A-Z][a-z])|\\b)");
    public static final int LIMIT = 2;
    public static final double SIMILARITY_THRESHOLD = 0.9;
    private static final Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private static final String abbreviationsCom = "https://www.abbreviations.com/";
    private static final String acronymFinderCom = "https://www.acronymfinder" + ".com/Information-Technology/";
    private static AbbreviationDisambiguationHelper instance;
    private static LinkedHashMap<String, String> ambiguated = new LinkedHashMap<>();
    private static final MutableMap<String, Disambiguation> local = Maps.mutable.empty();

    protected static synchronized @NotNull AbbreviationDisambiguationHelper getInstance() {
        if (instance == null) {
            instance = new AbbreviationDisambiguationHelper();
        }
        return instance;
    }

    public AbbreviationDisambiguationHelper() {
        super("abbreviations", ".json", "");
    }

    public static void addTransient(Disambiguation disambiguation) {
        local.merge(disambiguation.getAbbreviation(), disambiguation, Disambiguation::addMeanings);
        ambiguated = new LinkedHashMap<>();
    }

    public static void addPersistent(Disambiguation disambiguation) {
        getPersistent().merge(disambiguation.getAbbreviation(), disambiguation, Disambiguation::addMeanings);
        ambiguated = new LinkedHashMap<>();
    }

    public static Set<String> disambiguate(@NotNull String abbreviation) {
        //Specifically check. We do not want to mess up our files.
        Objects.requireNonNull(abbreviation);
        if (abbreviation.isBlank())
            throw new IllegalArgumentException();

        var fromTransientCache = getAll().getOrDefault(abbreviation, null);
        if (fromTransientCache != null)
            return fromTransientCache.getMeanings();

        var fromPersistentCache = getPersistent().getOrDefault(abbreviation, null);
        if (fromPersistentCache != null)
            return fromPersistentCache.getMeanings();

        var fromCrawl = crawl(abbreviation);
        addPersistent(fromCrawl);

        return fromCrawl.getMeanings();
    }

    /**
     * Replaces all meanings with their known abbreviation in a single string.
     *
     * @param text       a text containing an arbitrary amount of meanings (can be zero)
     * @param ignoreCase whether to ignore the casing when searching for a meaning inside the text
     * @return a single string where all meanings have been replaced with known abbreviations
     */
    public static String ambiguateAll(@NotNull String text, boolean ignoreCase) {
        return ambiguated.computeIfAbsent(text, (key) -> replaceAllMeanings(key, ignoreCase));
    }

    private static String replaceAllMeanings(@NotNull String text, boolean ignoreCase) {
        var replaced = text;
        var disambiguations = getAll();
        for (var disambiguation : disambiguations) {
            replaced = disambiguation.replaceMeaningWithAbbreviation(replaced, ignoreCase);
        }
        return replaced;
    }

    public static Set<String> ambiguate(@NotNull String meaning) {
        var set = new HashSet<String>();
        var crossProduct = similarAbbreviations(meaning);
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

    private static MutableMap<String, Disambiguation> getPersistent() {
        return getInstance().getOrRead();
    }

    public static ImmutableMap<String, Disambiguation> getAll() {
        return Disambiguation.merge(local, getPersistent()).toImmutable();
    }

    public void add(@NotNull Disambiguation disambiguation) {
        //Specifically check. We do not want to mess up our files.
        Objects.requireNonNull(disambiguation);
        Objects.requireNonNull(disambiguation.getAbbreviation());
        Objects.requireNonNull(disambiguation.getMeanings());
        if (disambiguation.getAbbreviation().isBlank() || disambiguation.getMeanings().isEmpty() || disambiguation.getMeanings()
                .stream()
                .anyMatch(String::isBlank))
            throw new IllegalArgumentException();
        var disambiguations = getOrRead();
        disambiguations.merge(disambiguation.getAbbreviation(), disambiguation, Disambiguation::addMeanings);
        cache(disambiguations);
    }

    private static List<List<Pair<String, String>>> similarAbbreviations(@NotNull String meaning) {
        var disambiguations = getAll();
        var allAbbrevs = new ArrayList<List<Pair<String, String>>>();
        for (var disambiguation : disambiguations) {
            var listOfPairs = disambiguation.getMeanings()
                    .stream()
                    .map(m -> new Pair<>(WordSimUtils.getSimilarity(m, meaning), m))
                    .filter(p -> p.first() >= SIMILARITY_THRESHOLD)
                    .map(p -> new Pair<>(disambiguation.getAbbreviation(), p.second()))
                    .toList();
            if (listOfPairs.isEmpty())
                continue;
            allAbbrevs.add(listOfPairs);
        }
        return com.google.common.collect.Lists.cartesianProduct(allAbbrevs).stream().filter(l -> !l.isEmpty()).toList();
    }

    public static Disambiguation crawl(@NotNull String abbreviation) {
        logger.info("Using crawler to disambiguate {}", abbreviation);
        var meanings = crawlAcronymFinderCom(abbreviation);
        meanings.addAll(crawlAbbreviationsCom(abbreviation));
        return new Disambiguation(abbreviation, meanings);
    }

    public static LinkedHashSet<String> crawlAbbreviationsCom(@NotNull String abbreviation) {
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

    public static LinkedHashSet<String> crawlAcronymFinderCom(@NotNull String abbreviation) {
        var meanings = new LinkedHashSet<String>();
        try {
            Document document = Jsoup.connect(acronymFinderCom + abbreviation + ".html").get();
            var elements = document.select("td.result-list__body__meaning > a, td" + ".result-list__body__meaning");
            meanings.addAll(elements.stream().limit(LIMIT).map(Element::text).map(AbbreviationDisambiguationHelper::removeAllBrackets).toList());
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
    protected @NotNull MutableMap<String, Disambiguation> read() throws CacheException {
        try {
            logger.info("Reading abbreviations file");
            var read = toMutableMap(new ObjectMapper().readValue(getFile(), Disambiguation[].class));
            logger.info("Found {} cached abbreviation", read.keysView().size());
            return read;
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public MutableMap<String, Disambiguation> getDefault() {
        return Maps.mutable.empty();
    }

    @Override
    protected void write(MutableMap<String, Disambiguation> content) {
        Collection<Disambiguation> values = content.valuesView().toList();
        try (PrintWriter out = new PrintWriter(getFile())) {
            //Parse before writing to the file, so we don't mess up the entire file due to a parsing error
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(values);
            out.print(json);
            logger.info("Saved abbreviations file");
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        }
    }

    private static @NotNull MutableMap<String, Disambiguation> toMutableMap(@NotNull Disambiguation[] abbreviations) {
        var all = new HashMap<String, Disambiguation>();
        for (var abbr : abbreviations) {
            all.put(abbr.getAbbreviation(), abbr);
        }
        return Maps.mutable.ofMap(all);
    }

    private static @NotNull String removeAllBrackets(String text) {
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
    private static @NotNull String removeBracket(String text) {
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

    /**
     * Uses the regex {@link #abbreviationsPattern} to find a set of possible abbreviations contained in the specified text.
     *
     * @param text the text
     * @return a set of possible abbreviations
     */
    public static @NotNull Set<String> getPossibleAbbreviations(String text) {
        var matcher = abbreviationsPattern.matcher(text);
        return new LinkedHashSet<>(matcher.results().map(MatchResult::group).toList());
    }

    /**
     * {@return whether the initialism candidate is an initialism of the text}
     *
     * @param text                the text
     * @param initialismCandidate the initialism candidate
     * @param initialismThreshold the percentage of characters in a word that need to be uppercase for a word to be considered an initialism candidate
     */
    public static boolean isInitialismOf(@NotNull String text, @NotNull String initialismCandidate, double initialismThreshold) {
        if (!couldBeAbbreviation(initialismCandidate, initialismThreshold))
            return false;

        //Check if the entire Initialism is contained within the single word
        if (!text.contains(" "))
            return shareInitial(text, initialismCandidate) && containsAllInOrder(text, initialismCandidate);

        StringBuilder reg = new StringBuilder();
        var initialLcArray = initialismCandidate.toCharArray();
        for (var c : initialLcArray) {
            reg.append(c).append("|");
        }

        var onlyInitialismLettersAndBlank = "\\[^(" + reg + "\\s)\\]";
        var split = text.split("\\s+");
        var reducedText = Arrays.stream(split).filter(s -> s.startsWith(onlyInitialismLettersAndBlank)).reduce("", (l, r) -> l + r);

        //The text contains words that are irrelevant to the supposed Initialism
        if (reducedText.length() != split.length)
            return false;

        return containsAllInOrder(reducedText, initialismCandidate);
    }

    /**
     * {@return whether the text could be an abbreviation} Compares the share of uppercase letters to the threshold.
     *
     * @param candidate the initialism candidate
     * @param threshold the initialism threshold
     */
    public static boolean couldBeAbbreviation(@NotNull String candidate, double threshold) {
        if (candidate.isEmpty())
            return false;
        var upperCaseCharacters = 0;
        var cArray = candidate.toCharArray();
        for (char c : cArray) {
            if (Character.isUpperCase(c))
                upperCaseCharacters++;
        }
        return upperCaseCharacters >= threshold * candidate.length();
    }

    /**
     * {@return whether the text contains all characters of the query in order} The characters do not have to be adjacent and can be separated by any amount of
     * characters.
     *
     * @param text  the text
     * @param query the query
     */
    public static boolean containsAllInOrder(@NotNull String text, @NotNull String query) {
        return containsInOrder(text, query) == query.length();
    }

    /**
     * {@return how many characters of the query are in order} The characters do not have to be adjacent and can be separated by any amount of characters. If a
     * character is not in order, it is disregarded.
     *
     * @param text  the text
     * @param query the query
     */
    public static long containsInOrder(@NotNull String text, @NotNull String query) {
        return Math.round(maximumAbbreviationScore(text, query, 0, 0, 1, 0));
    }

    public static double maximumAbbreviationScore(@NotNull String text, @NotNull String abbreviation, double rewardInitialMatch, double rewardAnyMatch,
            double rewardCaseMatch, int textIndex) {
        if (abbreviation.isEmpty() || textIndex >= text.length())
            return 0;
        var current = abbreviation.substring(0, 1);
        var index = text.toLowerCase(Locale.US).indexOf(current.toLowerCase(Locale.US), textIndex);
        if (index == -1)
            return 0;
        var score = maximumAbbreviationScore(text, abbreviation.substring(1), rewardInitialMatch, rewardAnyMatch, rewardCaseMatch, index + 1);
        if (index == 0 || text.charAt(index - 1) == ' ') {
            score += rewardInitialMatch;
        }
        if (text.substring(index, index + 1).equals(current)) {
            score += rewardCaseMatch;
        }
        score += rewardAnyMatch;
        return Math.max(score, maximumAbbreviationScore(text, abbreviation, rewardInitialMatch, rewardAnyMatch, rewardCaseMatch, index + 1));
    }

    public static boolean shareInitial(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty())
            return false;
        return a.substring(0, 1).equals(b.substring(0, 1));
    }
}