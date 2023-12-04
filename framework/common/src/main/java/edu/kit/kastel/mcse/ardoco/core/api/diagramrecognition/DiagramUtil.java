/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.LinkedHashMap;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashSet;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.SimilarityStrategy;

/**
 * Provides utility methods that are shared by {@link edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant informants} of diagram related stages.
 */
public class DiagramUtil {
    private DiagramUtil() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    /**
     * Returns the map containing the highest similarity of each word contained by the recommended instance to any word contained by the box.
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param box                 the box the target words are extracted from
     * @param recommendedInstance the recommended instance the words are extracted from
     * @return the map
     * @see #calculateSimilarityMap(WordSimUtils, UnmodifiableLinkedHashSet, UnmodifiableLinkedHashSet)
     */
    public static @NotNull LinkedHashMap<Word, Double> calculateSimilarityMap(@NotNull WordSimUtils wordSimUtils, @NotNull Box box, @NotNull RecommendedInstance recommendedInstance) {
        var deNames = box.getReferences();
        var words = UnmodifiableLinkedHashSet.of(recommendedInstance.getNameMappings().stream().flatMap(nm -> nm.getWords().stream()).toList());
        return calculateSimilarityMap(wordSimUtils, words, deNames);
    }

    /**
     * {@return the map containing the highest similarity of each word to any word from the target words}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param words   the words
     * @param targets the target words
     */
    private static @NotNull LinkedHashMap<Word, Double> calculateSimilarityMap(@NotNull WordSimUtils wordSimUtils, @NotNull UnmodifiableLinkedHashSet<Word> words,
            @NotNull UnmodifiableLinkedHashSet<String> targets) {
        var map = new LinkedHashMap<Word, Double>();
        words.forEach(w -> map.put(w, calculateHighestSimilarity(wordSimUtils, w, targets)));

        return new LinkedHashMap<>(map);
    }

    /**
     * {@return the highest similarity of the word to any word from the target words}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param word    the word
     * @param targets the target words
     */
    private static double calculateHighestSimilarity(@NotNull WordSimUtils wordSimUtils, @NotNull Word word, @NotNull UnmodifiableLinkedHashSet<String> targets) {
        return targets.stream()
                .map(name -> wordSimUtils.getSimilarity(word.getText(), name, SimilarityStrategy.MAXIMUM, true))
                .max(Double::compareTo)
                .orElse(Double.MIN_VALUE);
    }

    /**
     * {@return the highest similarity between a word from the noun mapping and a box reference}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param nounMapping the noun mapping
     * @param box         the box
     */
    public static double calculateHighestSimilarity(@NotNull WordSimUtils wordSimUtils, @NotNull NounMapping nounMapping, @NotNull Box box) {
        return nounMapping.getReferenceWords().stream().map(word -> calculateHighestSimilarity(wordSimUtils, word, box)).max(Double::compareTo).orElse(0.0);
    }

    /**
     * {@return the highest similarity between the word and a box reference}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param word the word
     * @param box  the box
     */
    public static double calculateHighestSimilarity(@NotNull WordSimUtils wordSimUtils, @NotNull Word word, @NotNull Box box) {
        return calculateHighestSimilarity(wordSimUtils, word, box.getReferences());
    }
}
