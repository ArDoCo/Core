/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashMap;
import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashSet;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;

/**
 * Provides functions to identify and disambiguate abbreviations. Caches results divided into a persistent {@link FileBasedCache} and a transient cache that is
 * deleted on program exit. Abbreviations and their meanings are encapsulated as {@link Disambiguation}. The {@link FileBasedCache} is implemented in JSON and
 * the file is saved in the user data directory folder of ArDoCo. The helper can be used to disambiguate an abbreviation using online abbreviation directory
 * lookups. Such disambiguations are saved in the persistent cache. The transient cache is populated by the stages. When comparing two words, it is generally
 * advised to ambiguate both rather than disambiguating.
 */
public final class AbbreviationDisambiguationHelper extends FileBasedCache<LinkedHashMap<String, Disambiguation>> {
    /**
     * Matches abbreviations with up to 1 lowercase letter between uppercase letters. Accounts for camelCase by lookahead, e.g. UserDBAdapter is matched as "DB"
     * rather than "DBA". Matches abbreviations at any point in the word, including at the start and end.
     */
    private final static Pattern abbreviationsPattern = Pattern.compile("(?:([A-Z]+[a-z]?)+[A-Z])(?=([A-Z][a-z])|\\b)");
    /*
    (?:([A-Z]+[a-z]?)+(?(?<=[A-Z])\w|[A-Z]))(?=([A-Z][a-z])|\b)
    potential improvement, can also match ArDoCo for example, assumes that two letters with first letter capital is an abbr. such as Id, Db..
     */

    public static final int LIMIT = 2;
    public static final double SIMILARITY_THRESHOLD = 0.9;
    private static final Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private static final String abbreviationsCom = "https://www.abbreviations.com/";
    private static final String acronymFinderCom = "https://www.acronymfinder.com/Information-Technology/";
    private static AbbreviationDisambiguationHelper instance;
    private static LinkedHashMap<String, String> ambiguated = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Disambiguation> local = new LinkedHashMap<>();

    /**
     * {@return the singleton instance of this class}
     */
    static synchronized @NotNull AbbreviationDisambiguationHelper getInstance() {
        if (instance == null) {
            instance = new AbbreviationDisambiguationHelper();
        }
        return instance;
    }

    private AbbreviationDisambiguationHelper() {
        super("abbreviations", ".json", "");
    }

    /**
     * Adds a disambiguation to the transient cache. If the abbreviation already exists, the disambiguations are merged instead.
     *
     * @param disambiguation the disambiguation
     */
    public static void addTransient(Disambiguation disambiguation) {
        local.merge(disambiguation.getAbbreviation(), disambiguation, Disambiguation::addMeanings);
        ambiguated = new LinkedHashMap<>();
    }

    /**
     * Adds a disambiguation to the persistent cache. If the abbreviation already exists, the disambiguations are merged instead.
     *
     * @param disambiguation the disambiguation
     */
    private static void addPersistent(Disambiguation disambiguation) {
        //Specifically check. We do not want to mess up our files.
        Objects.requireNonNull(disambiguation);
        Objects.requireNonNull(disambiguation.getAbbreviation());
        Objects.requireNonNull(disambiguation.getMeanings());
        if (disambiguation.getAbbreviation().isBlank() || disambiguation.getMeanings().isEmpty() || disambiguation.getMeanings()
                .stream()
                .anyMatch(String::isBlank))
            return;
        var disambiguations = getPersistent();
        disambiguations.merge(disambiguation.getAbbreviation(), disambiguation, Disambiguation::addMeanings);
        getInstance().cache(disambiguations);
        ambiguated = new LinkedHashMap<>();
    }

    /**
     * Tries to disambiguate the provided abbreviation and returns the potentially empty set of meanings
     *
     * @param abbreviation the abbreviation
     * @return a set of meanings
     */
    public static @NotNull UnmodifiableLinkedHashSet<String> disambiguate(@NotNull String abbreviation) {
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
     * Replaces all meanings with their known abbreviation in a single string. The result is cached.
     *
     * @param text       a text containing an arbitrary amount of meanings (can be zero)
     * @param ignoreCase whether to ignore the casing when searching for a meaning inside the text
     * @return a single string where all meanings have been replaced with known abbreviations
     */
    public static String ambiguateAll(@NotNull String text, boolean ignoreCase) {
        return ambiguated.computeIfAbsent(text, (key) -> replaceAllMeanings(key, ignoreCase));
    }

    /**
     * Replaces all meanings with their known abbreviation in a single string. For example, "Personal Computer Database" -> "PC DB"
     *
     * @param text       a text containing an arbitrary amount of meanings (can be zero)
     * @param ignoreCase whether to ignore the casing when searching for a meaning inside the text
     * @return a single string where all meanings have been replaced with known abbreviations
     */
    private static String replaceAllMeanings(@NotNull String text, boolean ignoreCase) {
        var replaced = text;
        var disambiguations = getAll().values();
        for (var disambiguation : disambiguations) {
            replaced = disambiguation.replaceMeaningWithAbbreviation(replaced, ignoreCase);
        }
        return replaced;
    }

    /**
     * Partial ambiguations of a text.
     *
     * @param text the text
     * @return a set of partially ambiguated strings
     */
    //TODO Check if this can be removed, may no longer be required due to how ambiguations are treated by the similarity metrics
    public static UnmodifiableLinkedHashSet<String> ambiguate(@NotNull String text) {
        var set = new LinkedHashSet<String>();
        var crossProduct = similarAbbreviations(text);
        if (crossProduct.isEmpty())
            return UnmodifiableLinkedHashSet.of(set);
        for (var abbreviationPairs : crossProduct) {
            var tempText = text;
            for (var pair : abbreviationPairs) {
                tempText = tempText.replace(pair.second().toLowerCase(), pair.first().toLowerCase());
            }
            set.add(tempText);
        }
        return UnmodifiableLinkedHashSet.of(set);
    }

    private static LinkedHashMap<String, Disambiguation> getPersistent() {
        return getInstance().getOrRead();
    }

    /**
     * {@return all disambiguations merged from both caches}
     */
    public static UnmodifiableLinkedHashMap<String, Disambiguation> getAll() {
        return new UnmodifiableLinkedHashMap<>(Disambiguation.merge(new UnmodifiableLinkedHashMap<>(local), new UnmodifiableLinkedHashMap<>(getPersistent())));
    }

    private static List<List<Pair<String, String>>> similarAbbreviations(@NotNull String meaning) {
        var disambiguations = getAll().values();
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

    /**
     * Crawls online abbreviation dictionaries for the abbreviation and combines their results.
     *
     * @param abbreviation the abbreviation
     * @return a potentially empty set of meanings
     */
    static Disambiguation crawl(@NotNull String abbreviation) {
        logger.info("Using crawler to disambiguate {}", abbreviation);
        var meanings = crawlAcronymFinderCom(abbreviation);
        meanings.addAll(crawlAbbreviationsCom(abbreviation));
        return new Disambiguation(abbreviation, meanings);
    }

    /**
     * Crawls abbreviations.com for the specified abbreviation.
     *
     * @param abbreviation the abbreviation
     * @return a potentially empty set of meanings
     */
    static LinkedHashSet<String> crawlAbbreviationsCom(@NotNull String abbreviation) {
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

    /**
     * Crawls acronymfinder.com for the specified abbreviation.
     *
     * @param abbreviation the abbreviation
     * @return a potentially empty set of meanings
     */
    static LinkedHashSet<String> crawlAcronymFinderCom(@NotNull String abbreviation) {
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

    @Override
    protected @NotNull LinkedHashMap<String, Disambiguation> read() throws CacheException {
        try {
            logger.info("Reading abbreviations file");
            var read = toLinkedHashMap(new ObjectMapper().readValue(getFile(), Disambiguation[].class));
            logger.info("Found {} cached abbreviation", read.size());
            return read;
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public LinkedHashMap<String, Disambiguation> getDefault() {
        return new LinkedHashMap<>();
    }

    @Override
    protected void write(LinkedHashMap<String, Disambiguation> content) {
        Collection<Disambiguation> values = content.values();
        try (PrintWriter out = new PrintWriter(getFile())) {
            //Parse before writing to the file, so we don't mess up the entire file due to a parsing error
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(values);
            out.print(json);
            logger.info("Saved abbreviations file");
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        }
    }

    private static @NotNull LinkedHashMap<String, Disambiguation> toLinkedHashMap(@NotNull Disambiguation[] abbreviations) {
        var all = new LinkedHashMap<String, Disambiguation>();
        for (var abbr : abbreviations) {
            all.put(abbr.getAbbreviation(), abbr);
        }
        return all;
    }

    /**
     * Removes all brackets and the text between them, does not support nested brackets
     *
     * @param text the text
     * @return the text without brackets
     */
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
     * Removes the first bracket and text between, does not support nested brackets
     *
     * @param text the text
     * @return the text without the first bracket
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
    public static @NotNull UnmodifiableLinkedHashSet<String> getAbbreviationCandidates(String text) {
        var matcher = abbreviationsPattern.matcher(text);
        return UnmodifiableLinkedHashSet.of(matcher.results().map(MatchResult::group).toList());
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

    /**
     * Calculates a score for how well the abbreviation matches the candidate meaning. A higher score indicates better. This function searches all character
     * sequences in the meaning which match the abbreviation. Each character of the sequence is rewarded based on the provided parameters and their conditions.
     * The maximum result of all such sequences is returned. For example, consider abbrev:"DB" and meaning:"Database". The sequence of characters which match
     * the abbreviation are at index 0 and 4. Both characters are rewarded with anyMatch. Character 0 is additionally rewarded with initialMatch, because it is
     * at a word boundary and with caseMatch, because its letter cases matches the abbreviation character. Thus, the sequence has a score of rewardInitialMatch
     * + rewardCaseMatch + 2 * rewardAnyMatch. If another sequence with a higher score existed, its value would be returned instead.
     *
     * @param meaningCandidate   the candidate meaning
     * @param abbreviation       the abbreviation
     * @param rewardInitialMatch >= 0
     * @param rewardAnyMatch     >= 0
     * @param rewardCaseMatch    >= 0
     * @param textIndex          start index, used for recursion, usually 0 at start
     * @return the score >= 0
     */
    public static double maximumAbbreviationScore(@NotNull String meaningCandidate, @NotNull String abbreviation, double rewardInitialMatch,
            double rewardAnyMatch, double rewardCaseMatch, int textIndex) {
        if (abbreviation.isEmpty() || textIndex >= meaningCandidate.length())
            return 0;
        var current = abbreviation.substring(0, 1);
        var index = meaningCandidate.toLowerCase(Locale.ENGLISH).indexOf(current.toLowerCase(Locale.ENGLISH), textIndex);
        if (index == -1)
            return 0;
        var score = maximumAbbreviationScore(meaningCandidate, abbreviation.substring(1), rewardInitialMatch, rewardAnyMatch, rewardCaseMatch, index + 1);
        if (index == 0 || meaningCandidate.charAt(index - 1) == ' ') {
            score += rewardInitialMatch;
        }
        if (meaningCandidate.substring(index, index + 1).equals(current)) {
            score += rewardCaseMatch;
        }
        score += rewardAnyMatch;
        return Math.max(score, maximumAbbreviationScore(meaningCandidate, abbreviation, rewardInitialMatch, rewardAnyMatch, rewardCaseMatch, index + 1));
    }

    /**
     * Whether the two string share the same initial.
     *
     * @param a first string
     * @param b second string
     * @return true if yes, otherwise false
     */
    public static boolean shareInitial(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty())
            return false;
        return a.substring(0, 1).equals(b.substring(0, 1));
    }
}
