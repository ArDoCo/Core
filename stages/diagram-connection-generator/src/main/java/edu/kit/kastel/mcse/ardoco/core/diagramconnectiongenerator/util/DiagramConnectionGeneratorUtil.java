package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DBPediaHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.SimilarityStrategy;

/**
 * Provides utility methods that are shared by {@link edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant informants} of this stage.
 */
public class DiagramConnectionGeneratorUtil {
    /**
     * Matches abbreviations with up to 1 lowercase letter between uppercase letters. Accounts for camelCase by lookahead, e.g. UserDBAdapter is matched as "DB"
     * rather than "DBA". Matches abbreviations at any point in the word, including at the start and end.
     */
    private final static Pattern abbreviationsPattern = Pattern.compile("(?:([A-Z]+[a-z]?)+[A-Z])(?=([A-Z][a-z])|\\b)");

    private DiagramConnectionGeneratorUtil() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    /**
     * Returns the map containing the highest similarity of each word contained by the recommended instance to any word contained by the box.
     *
     * @param box                 the box the target words are extracted from
     * @param recommendedInstance the recommended instance the words are extracted from
     * @return the map
     * @see #calculateHighestSimilarity(Set, Set)
     */
    public static @NotNull Map<Word, Double> calculateHighestSimilarity(@NotNull Box box, @NotNull RecommendedInstance recommendedInstance) {
        var deNames = getPossibleNames(box);
        var words = recommendedInstance.getNameMappings().stream().flatMap(nm -> nm.getWords().stream()).collect(Collectors.toSet());
        return calculateHighestSimilarity(words, deNames);
    }

    /**
     * {@return the map containing the highest similarity of each word to any word from the target words}
     *
     * @param words   the words
     * @param targets the target words
     */
    public static @NotNull Map<Word, Double> calculateHighestSimilarity(@NotNull Set<Word> words, @NotNull Set<String> targets) {
        return words.stream()
                .collect(Collectors.toMap(w -> w, w -> targets.stream()
                        .map(name -> WordSimUtils.getSimilarity(w.getPhrase().getText(), name, SimilarityStrategy.MAXIMUM, true))
                        .max(Double::compareTo)
                        .orElse(Double.MIN_VALUE)));
    }

    /**
     * {@return a set of alternative texts extracted from the input text}. The text is processed with {@link #splitBracketsAndEnumerations(String)} and
     * {@link #getDeCameledText(String)}.
     *
     * @param text the text
     */
    public static @NotNull Set<String> processText(@NotNull String text) {
        var words = new LinkedHashSet<String>();
        var split = splitBracketsAndEnumerations(text);
        var deCameledSplit = split.stream().map(DiagramConnectionGeneratorUtil::getDeCameledText).toList();
        words.addAll(split);
        words.addAll(deCameledSplit);
        words.remove("");
        return words;
    }

    /**
     * Splits the string around brackets and commas. The results are trimmed. <span style=" white-space: nowrap;">Example: "Lorem (ipsum), Dolor, sit (Amet)" ->
     * {"Lorem","ipsum","Dolor","sit","Amet"}</span>
     *
     * @param text the text
     * @return a non-empty list of splits
     */
    public static @NotNull List<String> splitBracketsAndEnumerations(@NotNull String text) {
        return Arrays.stream(text.split("[,()]")).map(String::trim).toList();
    }

    /**
     * Decamels the word and returns it as words joined by space. <span style=" white-space: nowrap;">Example: "CamelCaseExample" -> "Camel Case Example",
     * "example" -> "example", etc.</span>
     *
     * @param word the word that should be decameled
     * @return the decameled word
     */
    public static @NotNull String getDeCameledText(@NotNull String word) {
        return String.join(" ", word.split("(?<!([A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")).replaceAll("\\s+", " ");
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
     * Determines a set of possible names for a box by processing the associated
     * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox textboxes}. Tries to filter out technical terms using {@link DBPediaHelper}.
     *
     * @param box the box
     * @return a set of possible names
     */
    public static @NotNull Set<String> getPossibleNames(@NotNull Box box) {
        var names = new LinkedHashSet<String>();

        var texts = box.getTexts();
        for (var textBox : texts) {
            var text = textBox.getText();
            var splitAndDecameled = processText(text).stream()
                    .filter(s -> !DBPediaHelper.isWordMarkupLanguage(s))
                    .filter(s -> !DBPediaHelper.isWordProgrammingLanguage(s))
                    .filter(s -> !DBPediaHelper.isWordSoftware(s))
                    .toList();
            var abbreviations = getPossibleAbbreviations(text).stream().map(AbbreviationDisambiguationHelper.getInstance()::get).toList();
            var noBlank = splitAndDecameled.stream().map(s -> s.replaceAll("\\s+", "")).toList();
            names.addAll(splitAndDecameled);
            names.addAll(noBlank);
        }

        return names;
    }

    /**
     * {@return whether the initialism candidate is an initialism of the text}
     *
     * @param text                the text
     * @param initialism          the percentage of characters in a word that need to be uppercase for a word to be considered an initialism candidate
     * @param initialismThreshold the initialism threshold
     */
    public static boolean isInitialismOf(@NotNull String text, @NotNull String initialism, double initialismThreshold) {
        if (!couldBeInitialism(initialism, initialismThreshold))
            return false;

        var lc = text.toLowerCase();
        var initialLc = initialism.toLowerCase();

        //Check if the entire Initialism is contained within the single word
        if (!lc.contains(" "))
            return lc.startsWith(initialLc.substring(0, 1)) && containsAllInOrder(lc, initialLc);

        StringBuilder reg = new StringBuilder();
        var initialLcArray = initialLc.toCharArray();
        for (var c : initialLcArray) {
            reg.append(c).append("|");
        }

        var onlyInitialismLettersAndBlank = "\\[^(" + reg + "\\s)\\]";
        var split = lc.split("\\s+");
        var reducedText = Arrays.stream(split).filter(s -> s.startsWith(onlyInitialismLettersAndBlank)).reduce("", (l, r) -> l + r);

        //The text contains words that are irrelevant to the supposed Initialism
        if (reducedText.length() != split.length)
            return false;

        return containsAllInOrder(reducedText, initialLc);
    }

    /**
     * {@return whether the text could be an initialism} Compares the share of uppercase letters to the initialism threshold.
     *
     * @param text                the text
     * @param initialismThreshold the initialism threshold
     */
    public static boolean couldBeInitialism(@NotNull String text, double initialismThreshold) {
        if (text.isEmpty())
            return false;
        var upperCaseCharacters = 0;
        var cArray = text.toCharArray();
        for (char c : cArray) {
            if (Character.isUpperCase(c))
                upperCaseCharacters++;
        }
        return upperCaseCharacters >= initialismThreshold * text.length();
    }

    /**
     * {@return whether the text contains all characters of the query in order.} The characters do not have to be adjacent and can be separated by any amount of
     * characters.
     *
     * @param text  the text
     * @param query the query
     */
    public static boolean containsAllInOrder(@NotNull String text, @NotNull String query) {
        var previous = -1;
        var cArray = query.toCharArray();
        for (char c : cArray) {
            var current = text.indexOf(String.valueOf(c));
            if (current <= previous)
                return false;
            previous = current;
        }
        return true;
    }
}
