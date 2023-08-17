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
import com.google.common.collect.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;

public class AbbreviationDisambiguationHelper extends FileBasedCache<ImmutableMap<String, Disambiguation>> {
    /**
     * Matches abbreviations with up to 1 lowercase letter between uppercase letters. Accounts for camelCase by lookahead, e.g. UserDBAdapter is matched as "DB"
     * rather than "DBA". Matches abbreviations at any point in the word, including at the start and end.
     */
    private final static Pattern abbreviationsPattern = Pattern.compile("(?:([A-Z]+[a-z]?)+[A-Z])" + "(?=([A-Z][a-z])|\\b)");
    public static final int LIMIT = 2;
    public static final double SIMILARITY_THRESHOLD = 0.9;
    private static Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private static final String abbreviationsCom = "https://www.abbreviations.com/";
    private static final String acronymFinderCom = "https://www.acronymfinder" + ".com/Information-Technology/";
    private static AbbreviationDisambiguationHelper instance;
    private MutableMap<String, Disambiguation> abbreviationsCache;

    public static synchronized @NotNull AbbreviationDisambiguationHelper getInstance() {
        if (instance == null) {
            instance = new AbbreviationDisambiguationHelper();
        }
        return instance;
    }

    public AbbreviationDisambiguationHelper() {
        super("abbreviations", ".json", "");
    }

    public Set<String> disambiguate(@NotNull String abbreviation) {
        //Specifically check. We do not want to mess up our files.
        Objects.requireNonNull(abbreviation);
        if (abbreviation.isBlank())
            throw new IllegalArgumentException();

        var disambiguation = loadMutable(true).computeIfAbsent(abbreviation, this::crawl);
        return Set.copyOf(disambiguation.getMeanings());
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

    public void add(@NotNull Disambiguation disambiguation) {
        //Specifically check. We do not want to mess up our files.
        Objects.requireNonNull(disambiguation);
        Objects.requireNonNull(disambiguation.getAbbreviation());
        Objects.requireNonNull(disambiguation.getMeanings());
        if (disambiguation.getAbbreviation().isBlank() || disambiguation.getMeanings().isEmpty() || disambiguation.getMeanings()
                .stream()
                .anyMatch(String::isBlank))
            throw new IllegalArgumentException();
        var disambiguations = loadMutable(true);
        disambiguations.merge(disambiguation.getAbbreviation(), disambiguation, Disambiguation::addMeanings);
        save(disambiguations);
    }

    private List<List<Pair<String, String>>> similarAbbreviations(@NotNull String meaning) {
        var disambiguations = load();
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
        return Lists.cartesianProduct(allAbbrevs).stream().filter(l -> !l.isEmpty()).toList();
    }

    public Disambiguation crawl(@NotNull String abbreviation) {
        logger.info("Using crawler to disambiguate {}", abbreviation);
        var meanings = crawlAcronymFinderCom(abbreviation);
        meanings.addAll(crawlAbbreviationsCom(abbreviation));
        return new Disambiguation(abbreviation, meanings);
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
            var elements = document.select("td.result-list__body__meaning > a, td" + ".result-list__body__meaning");
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
    public @NotNull ImmutableMap<String, Disambiguation> load(boolean allowReload) {
        return Maps.immutable.ofMap(loadMutable(allowReload));
    }

    @Override
    public Optional<ImmutableMap<String, Disambiguation>> get() {
        return Optional.ofNullable(abbreviationsCache).map(MutableMap::toImmutable);
    }

    /**
     * Gets the abbreviation file from the disk and parses its content.
     *
     * @return the abbreviations
     */
    protected @NotNull MutableMap<String, Disambiguation> loadMutable(boolean allowReload) {
        if (abbreviationsCache != null)
            return abbreviationsCache;
        try {
            logger.info("Reading abbreviations file");
            abbreviationsCache = toMutableMap(new ObjectMapper().readValue(getFile(), Disambiguation[].class));
            logger.info("Found {} cached abbreviation", abbreviationsCache.keySet().size());
            return abbreviationsCache;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ImmutableMap<String, Disambiguation> getDefault() {
        return Maps.immutable.empty();
    }

    @Override
    public void save(ImmutableMap<String, Disambiguation> content) {
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

    private void save(MutableMap<String, Disambiguation> content) {
        save(Maps.immutable.ofMap(content));
    }

    private @NotNull MutableMap<String, Disambiguation> toMutableMap(@NotNull Disambiguation[] abbreviations) {
        var all = new HashMap<String, Disambiguation>();
        for (var abbr : abbreviations) {
            all.put(abbr.getAbbreviation(), abbr);
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
