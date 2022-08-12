/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;

/**
 * This class is a utility class.
 *
 */
public final class SimilarityUtils {

    private SimilarityUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Checks the similarity of two {@link NounMapping}s.
     *
     * @param nm1 the first NounMapping
     * @param nm2 the second NounMapping
     * @return true, if the {@link NounMapping}s are similar; false if not.
     */
    public static boolean areNounMappingsSimilar(NounMapping nm1, NounMapping nm2) {
        var nm1Words = nm1.getReferenceWords();
        var nm2Words = nm2.getReferenceWords();
        var nm1Reference = nm1.getReference();
        var nm2Reference = nm2.getReference();
        var nm1SplitAtSeparators = CommonUtilities.splitAtSeparators(nm1Reference);
        var nm2SplitAtSeparators = CommonUtilities.splitAtSeparators(nm2Reference);

        if (nm1SplitAtSeparators.isEmpty() || nm2SplitAtSeparators.isEmpty()) {
            return false;
        }
        var nm1FirstPart = nm1SplitAtSeparators.get(0);
        var nm2FirstPart = nm2SplitAtSeparators.get(0);

        if (nm1Words.size() == 1 && nm2Words.size() == 1) {
            var nm1Word = nm1Words.get(0);
            var nm2Word = nm2Words.get(0);
            return areWordsSimilar(nm1FirstPart, nm2FirstPart) || areWordsSimilar(nm1Word, nm2Word);
        }
        return areWordsSimilar(nm1Reference, nm2Reference);
    }

    /**
     * Compares a given {@link NounMapping} with a given {@link ModelInstance} for similarity. Checks if all names, the
     * longest name or a single name are similar to the reference of the NounMapping.
     *
     * @param nounMapping the {@link NounMapping}
     * @param instance    the {@link ModelInstance}
     * @return true, iff the {@link NounMapping} and {@link ModelInstance} are similar.
     */
    public static boolean isNounMappingSimilarToModelInstance(NounMapping nounMapping, ModelInstance instance) {
        if (areWordsOfListsSimilar(instance.getNameParts(), Lists.immutable.with(nounMapping.getReference())) || areWordsSimilar(instance.getFullName(),
                nounMapping.getReference())) {
            return true;
        }

        for (String name : instance.getNameParts()) {
            if (areWordsSimilar(name, nounMapping.getReference())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares a given {@link Word} with a given {@link ModelInstance} for similarity.
     *
     * @param word     the {@link Word}
     * @param instance the {@link ModelInstance}
     * @return true, iff the {@link Word} and {@link ModelInstance} are similar.
     */
    public static boolean isWordSimilarToModelInstance(Word word, ModelInstance instance) {
        var names = instance.getNameParts();
        return compareWordWithStringListEntries(word, names);
    }

    /**
     * Compares a given {@link RecommendedInstance} with a given {@link ModelInstance} for similarity.
     *
     * @param ri       the {@link RecommendedInstance}
     * @param instance the {@link ModelInstance}
     * @return true, iff the {@link RecommendedInstance} and {@link ModelInstance} are similar.
     */
    public static boolean isRecommendedInstanceSimilarToModelInstance(RecommendedInstance ri, ModelInstance instance) {
        var name = ri.getName();
        var nameList = Lists.immutable.with(name.split(" "));
        return instance.getFullName().equalsIgnoreCase(ri.getName()) || areWordsOfListsSimilar(instance.getNameParts(), nameList);
    }

    /**
     * Compares a given {@link Word} with the type of a given {@link ModelInstance} for similarity.
     *
     * @param word     the {@link Word}
     * @param instance the {@link ModelInstance}
     * @return true, iff the {@link Word} and the type of the {@link ModelInstance} are similar.
     */
    public static boolean isWordSimilarToModelInstanceType(Word word, ModelInstance instance) {
        var types = instance.getTypeParts();
        return compareWordWithStringListEntries(word, types);
    }

    private static boolean compareWordWithStringListEntries(Word word, ImmutableList<String> names) {
        if (areWordsOfListsSimilar(names, Lists.immutable.with(word.getText()))) {
            return true;
        }

        for (String name : names) {
            if (areWordsSimilar(name, word.getText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the similarity of two {@link Word}s.
     *
     * @param word1 the first word
     * @param word2 the second word
     * @return true, if the words are similar; false if not.
     */
    public static boolean areWordsSimilar(Word word1, Word word2) {
        var word1Text = word1.getText();
        var word2Text = word2.getText();
        return areWordsSimilar(word1Text, word2Text);
    }

    /**
     * Checks the similarity of two string. Uses Jaro-Winkler similarity and Levenshtein to assess the similarity.
     *
     * @param word1 String of first word
     * @param word2 String of second word
     * @return true, if the test string is similar to the original; false if not.
     */
    public static boolean areWordsSimilar(String word1, String word2) {
        return WordSimUtils.areWordsSimilar(word1, word2);
    }

    /**
     * Checks the similarity of a list with test strings to a list of "original" strings. In this method all test
     * strings are compared to all originals. For this the method uses the areWordsSimilar method with a given
     * threshold. All matches are counted. If the proportion of similarities between the lists is greater than the given
     * threshold the method returns true.
     *
     * @param originals     list of original strings
     * @param words2test    list of test strings
     * @param minProportion threshold for proportional similarity between the lists
     * @return true if the list are similar, false if not
     */
    public static boolean areWordsOfListsSimilar(ImmutableList<String> originals, ImmutableList<String> words2test, double minProportion) {

        if (areWordsSimilar(String.join(" ", originals), String.join(" ", words2test))) {
            return true;
        }

        var counter = 0;
        for (String o : originals) {
            for (String wd : words2test) {
                if (areWordsSimilar(o, wd)) {
                    counter++;
                }
            }
        }

        return 1.0 * counter / Math.max(originals.size(), words2test.size()) >= minProportion;
    }

    /**
     * Checks the similarity of a list, containing test strings, and a list of originals. This check is not
     * bidirectional! This method uses the areWordsSimilar method with a given threshold.
     *
     * @param originals  list of original strings
     * @param words2test list of test strings
     * @return true if the list are similar, false if not
     */
    public static boolean areWordsOfListsSimilar(ImmutableList<String> originals, ImmutableList<String> words2test) {
        return areWordsOfListsSimilar(originals, words2test, CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD);
    }

    /**
     * Extracts most likely matches of a list of recommended instances by similarity to a given instance. For this, the
     * method uses an increasing minimal proportional threshold with the method areWordsOfListsSimilar method. If all
     * lists are similar to the given instance by a threshold of 1-increase value the while loop can be left. If the
     * while loop ends with more than one possibility or all remaining lists are sorted out in the same run, all are
     * returned. Elsewhere only the remaining recommended instance is returned within the list.
     *
     * @param instance             instance to use as original for compare
     * @param recommendedInstances recommended instances to check for similarity
     * @return a list of the most similar recommended instances (to the instance names)
     */
    public static ImmutableList<RecommendedInstance> getMostRecommendedInstancesToInstanceByReferences(ModelInstance instance,
            ImmutableList<RecommendedInstance> recommendedInstances) {
        var instanceNames = instance.getNameParts();
        var similarity = CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD;
        var selection = recommendedInstances.select(ri -> checkRecommendedInstanceForSelection(instance, ri, similarity));

        var getMostRecommendedIByRefMinProportion = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION;
        var getMostRecommendedIByRefIncrease = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_INCREASE;

        MutableList<RecommendedInstance> whileSelection = Lists.mutable.withAll(selection);
        var allListsSimilar = 0;

        while (whileSelection.size() > 1 && getMostRecommendedIByRefMinProportion <= 1) {
            selection = Lists.immutable.withAll(whileSelection);
            getMostRecommendedIByRefMinProportion += getMostRecommendedIByRefIncrease;
            MutableList<RecommendedInstance> risToRemove = Lists.mutable.empty();
            for (RecommendedInstance ri : whileSelection) {
                if (checkRecommendedInstanceWordSimilarityToInstance(instance, ri)) {
                    allListsSimilar++;
                }

                if (!SimilarityUtils.areWordsOfListsSimilar(instanceNames, Lists.immutable.with(ri.getName()), getMostRecommendedIByRefMinProportion)) {
                    risToRemove.add(ri);
                }
            }
            whileSelection.removeAll(risToRemove);
            if (allListsSimilar == whileSelection.size()) {
                return whileSelection.toImmutable();
            }
            allListsSimilar = 0;
        }
        if (whileSelection.isEmpty()) {
            return selection;
        }
        return whileSelection.toImmutable();

    }

    private static boolean checkRecommendedInstanceWordSimilarityToInstance(ModelInstance instance, RecommendedInstance ri) {
        var instanceNames = instance.getNameParts();
        for (var sf : ri.getNameMappings().flatCollect(NounMapping::getSurfaceForms)) {
            var splitSF = CommonUtilities.splitCases(String.join(" ", CommonUtilities.splitAtSeparators(sf)));
            if (areWordsSimilar(String.join(" ", instanceNames), splitSF)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkRecommendedInstanceForSelection(ModelInstance instance, RecommendedInstance ri, double similarity) {
        var instanceNames = instance.getNameParts();
        ImmutableList<String> longestNameSplit = Lists.immutable.of(CommonUtilities.splitCases(instance.getFullName()).split(" "));
        ImmutableList<String> recommendedInstanceNameList = Lists.immutable.with(ri.getName());
        if (areWordsSimilar(instance.getFullName(), ri.getName()) || SimilarityUtils.areWordsOfListsSimilar(instanceNames, recommendedInstanceNameList,
                similarity) || SimilarityUtils.areWordsOfListsSimilar(longestNameSplit, recommendedInstanceNameList, similarity)) {
            return true;
        }
        for (var nounMapping : ri.getNameMappings()) {
            for (var surfaceForm : nounMapping.getSurfaceForms()) {
                var splitSurfaceForm = CommonUtilities.splitCases(surfaceForm);
                var surfaceFormWords = CommonUtilities.splitAtSeparators(splitSurfaceForm);
                if (SimilarityUtils.areWordsOfListsSimilar(instanceNames, surfaceFormWords, similarity) || SimilarityUtils.areWordsOfListsSimilar(
                        longestNameSplit, surfaceFormWords, similarity)) {
                    return true;
                }
            }
        }
        return false;
    }
}
