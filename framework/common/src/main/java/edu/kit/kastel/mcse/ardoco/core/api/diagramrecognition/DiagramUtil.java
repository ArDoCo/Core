package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import com.google.common.collect.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DbPediaHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.SimilarityStrategy;

/**
 * Provides utility methods that are shared by {@link edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant informants} of this stage.
 */
public class DiagramUtil {
    private DiagramUtil() {
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
        var deNames = box.getReferences();
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
        return words.stream().collect(Collectors.toMap(w -> w, w -> calculateHighestSimilarity(w, targets)));
    }

    /**
     * {@return the highest similarity of the word to any word from the target words}
     *
     * @param word    the word
     * @param targets the target words
     */
    public static double calculateHighestSimilarity(@NotNull Word word, @NotNull Set<String> targets) {
        return targets.stream()
                .map(name -> WordSimUtils.getSimilarity(word.getText(), name, SimilarityStrategy.MAXIMUM, true))
                .max(Double::compareTo)
                .orElse(Double.MIN_VALUE);
    }

    /**
     * {@return the similarity of the noun mapping to the box}
     *
     * @param nounMapping the noun mapping
     * @param box         the box
     */
    public static double calculateSimilarity(@NotNull NounMapping nounMapping, @NotNull Box box) {
        var targets = box.getReferences();
        return nounMapping.getReferenceWords().stream().map(word -> calculateHighestSimilarity(word, targets)).max(Double::compareTo).orElse(0.0);
    }

    /**
     * {@return the similarity of the word to the box}
     *
     * @param word the word
     * @param box  the box
     */
    public static double calculateSimilarity(@NotNull Word word, @NotNull Box box) {
        return calculateHighestSimilarity(word, box.getReferences());
    }

    /**
     * {@return a set of alternative texts extracted from the input text}. The text is processed with {@link #splitBracketsAndEnumerations(String)} and
     * {@link #getDeCameledText(String)}.
     *
     * @param text the text
     */
    private static @NotNull Set<String> processText(@NotNull String text) {
        var words = new LinkedHashSet<String>();
        var split = splitBracketsAndEnumerations(text);
        var deCameledSplit = split.stream().map(DiagramUtil::getDeCameledText).toList();
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
    private static @NotNull List<String> splitBracketsAndEnumerations(@NotNull String text) {
        return Arrays.stream(text.split("[,()]")).map(String::trim).toList();
    }

    /**
     * Decamels the word and returns it as words joined by space. <span style=" white-space: nowrap;">Example: "CamelCaseExample" -> "Camel Case Example",
     * "example" -> "example", etc.</span>
     *
     * @param word the word that should be decameled
     * @return the decameled word
     */
    private static @NotNull String getDeCameledText(@NotNull String word) {
        return String.join(" ", word.split("(?<!([A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")).replaceAll("\\s+", " ");
    }

    /**
     * Determines a set of possible names for a box by processing the associated
     * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox textboxes}. Tries to filter out technical terms using {@link DbPediaHelper}.
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
                    .filter(s -> !DbPediaHelper.isWordMarkupLanguage(s))
                    .filter(s -> !DbPediaHelper.isWordProgrammingLanguage(s))
                    .filter(s -> !DbPediaHelper.isWordSoftware(s))
                    .toList();
            var abbreviations = AbbreviationDisambiguationHelper.getPossibleAbbreviations(text);
            var meaningsMap = abbreviations.stream().collect(Collectors.toMap(a -> a, AbbreviationDisambiguationHelper.getInstance()::disambiguate));
            var crossProduct = Lists.cartesianProduct(
                    meaningsMap.entrySet().stream().map(s -> s.getValue().stream().map(v -> new Pair<>(s.getKey(), v)).toList()).toList());
            for (var meanings : crossProduct) {
                if (meanings.isEmpty())
                    continue;
                var textWithReplacements = text;
                for (Pair<String, String> replacement : meanings) {
                    textWithReplacements = textWithReplacements.replace(replacement.first(), replacement.second());
                }
                names.add(textWithReplacements);
            }
            var noBlank = splitAndDecameled.stream().map(s -> s.replaceAll("\\s+", "")).toList();
            names.addAll(splitAndDecameled);
            names.addAll(noBlank);
        }

        return names;
    }
}
