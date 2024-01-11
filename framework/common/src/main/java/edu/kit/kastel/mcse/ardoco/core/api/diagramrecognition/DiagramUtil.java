/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
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
     * @param wordSimUtils        the word similarity utils to use for comparison
     * @param box                 the box the target words are extracted from
     * @param recommendedInstance the recommended instance the words are extracted from
     * @return the map
     * @see #calculateSimilarityMap(WordSimUtils, SortedSet, SortedSet)
     */
    public static SortedMap<Word, Double> calculateSimilarityMap(WordSimUtils wordSimUtils, Box box, RecommendedInstance recommendedInstance) {
        var deNames = box.getReferences();
        var words = new TreeSet<>(recommendedInstance.getNameMappings().stream().flatMap(nm -> nm.getWords().stream()).toList());
        return calculateSimilarityMap(wordSimUtils, words, deNames);
    }

    /**
     * {@return the map containing the highest similarity of each word to any word from the target words}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param words        the words
     * @param targets      the target words
     */
    private static SortedMap<Word, Double> calculateSimilarityMap(WordSimUtils wordSimUtils, SortedSet<Word> words, SortedSet<String> targets) {
        var map = new TreeMap<Word, Double>();
        words.forEach(w -> map.put(w, calculateHighestSimilarity(wordSimUtils, w, targets)));

        return new TreeMap<>(map);
    }

    /**
     * {@return the highest similarity of the word to any word from the target words}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param word         the word
     * @param targets      the target words
     */
    private static double calculateHighestSimilarity(WordSimUtils wordSimUtils, Word word, SortedSet<String> targets) {
        return targets.stream()
                .map(name -> wordSimUtils.getSimilarity(word.getText(), name, SimilarityStrategy.MAXIMUM, true))
                .max(Double::compareTo)
                .orElse(Double.MIN_VALUE);
    }

    /**
     * {@return the highest similarity between a word from the noun mapping and a box reference}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param nounMapping  the noun mapping
     * @param box          the box
     */
    public static double calculateHighestSimilarity(WordSimUtils wordSimUtils, NounMapping nounMapping, Box box) {
        return nounMapping.getReferenceWords().stream().map(word -> calculateHighestSimilarity(wordSimUtils, word, box)).max(Double::compareTo).orElse(0.0);
    }

    /**
     * {@return the highest similarity between the word and a box reference}
     *
     * @param wordSimUtils the word similarity utils to use for comparison
     * @param word         the word
     * @param box          the box
     */
    public static double calculateHighestSimilarity(WordSimUtils wordSimUtils, Word word, Box box) {
        return calculateHighestSimilarity(wordSimUtils, word, box.getReferences());
    }
}
