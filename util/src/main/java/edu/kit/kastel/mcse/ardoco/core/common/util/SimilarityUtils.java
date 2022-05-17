/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.CloseableThreadContext.Instance;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;

/**
 * This class is a utility class.
 *
 * @author Sophie, Jan
 *
 */
public final class SimilarityUtils {

    private static LevenshteinDistance levenShteinDistance = new LevenshteinDistance();
    private static JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();

    private SimilarityUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Checks the similarity of two {@link INounMapping}s.
     *
     * @param nm1 the first NounMapping
     * @param nm2 the second NounMapping
     * @return true, if the {@link INounMapping}s are similar; false if not.
     */
    public static boolean areNounMappingsSimilar(INounMapping nm1, INounMapping nm2) {
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
        } else {
            return areWordsSimilar(nm1Reference, nm2Reference);
        }
    }

    // TODO: alle Stellen finden, an denen man dies ggf anwenden sollte
    /**
     * Compares a given {@link INounMapping} with a given {@link IModelInstance} for similarity. Checks if all names,
     * the longest name or a single name are similar to the reference of the NounMapping.
     *
     * @param nounMapping the {@link INounMapping}
     * @param instance    the {@link IModelInstance}
     * @return true, iff the {@link INounMapping} and {@link IModelInstance} are similar.
     */
    public static boolean isNounMappingSimilarToModelInstance(INounMapping nounMapping, IModelInstance instance) {
        if (areWordsOfListsSimilar(instance.getNameParts(), Lists.immutable.with(nounMapping.getReference()))
                || areWordsSimilar(instance.getFullName(), nounMapping.getReference())) {
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
     * Compares a given {@link IWord} with a given {@link IModelInstance} for similarity.
     *
     * @param word     the {@link IWord}
     * @param instance the {@link IModelInstance}
     * @return true, iff the {@link IWord} and {@link IModelInstance} are similar.
     */
    public static boolean isWordSimilarToModelInstance(IWord word, IModelInstance instance) {
        var names = instance.getNameParts();
        return compareWordWithStringListEntries(word, names);
    }

    /**
     * Compares a given {@link IRecommendedInstance} with a given {@link IModelInstance} for similarity.
     *
     * @param ri       the {@link IRecommendedInstance}
     * @param instance the {@link IModelInstance}
     * @return true, iff the {@link IRecommendedInstance} and {@link IModelInstance} are similar.
     */
    public static boolean isRecommendedInstanceSimilarToModelInstance(IRecommendedInstance ri, IModelInstance instance) {
        var name = ri.getName();
        var nameList = Lists.immutable.with(name.split(" "));
        return instance.getFullName().equalsIgnoreCase(ri.getName()) || areWordsOfListsSimilar(instance.getNameParts(), nameList);
    }

    /**
     * Compares a given {@link IWord} with the type of a given {@link IModelInstance} for similarity.
     *
     * @param word     the {@link IWord}
     * @param instance the {@link IModelInstance}
     * @return true, iff the {@link IWord} and the type of the {@link IModelInstance} are similar.
     */
    public static boolean isWordSimilarToModelInstanceType(IWord word, IModelInstance instance) {
        var types = instance.getTypeParts();
        return compareWordWithStringListEntries(word, types);
    }

    private static boolean compareWordWithStringListEntries(IWord word, ImmutableList<String> names) {
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
     * Checks the similarity of two {@link IWord}s.
     *
     * @param word1 the first word
     * @param word2 the second word
     * @return true, if the words are similar; false if not.
     */
    public static boolean areWordsSimilar(IWord word1, IWord word2) {
        var word1Text = word1.getText();
        var word2Text = word2.getText();
        //return areWordsSimilar(word1Text, word2Text);
        return WordSimUtils.areWordsSimilar(word1, word2);
    }

    /**
     * Checks the similarity of two string. Uses Jaro-Winkler similarity and Levenshtein to assess the similarity.
     *
     * @param word1 String of first word
     * @param word2 String of second word
     * @return true, if the test string is similar to the original; false if not.
     */
    public static boolean areWordsSimilar(String word1, String word2) {
        return areWordsSimilar(word1, word2, CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD);
    }

    private static boolean areWordsSimilar(String original, String word2test, double similarityThreshold) {
        if (original == null || word2test == null) {
            return false;
        }
        String originalLowerCase = original.toLowerCase();
        String word2TestLowerCase = word2test.toLowerCase();
        if (originalLowerCase.split(" ").length != word2TestLowerCase.split(" ").length) {
            return false;
        }

        //var isLevenshteinSimilar = levenshteinDistanceTest(original, word2test, similarityThreshold);
        //var isJaroWinklerSimilar = jaroWinklerSimilarityTest(original, word2test, similarityThreshold);
        //return isJaroWinklerSimilar || isLevenshteinSimilar;

        return WordSimUtils.areWordsSimilar(original, word2test);
    }

    private static boolean jaroWinklerSimilarityTest(String original, String word2test, Double threshold) {
        return jaroWinklerSimilarity.apply(original, word2test) >= threshold;
    }

    private static boolean levenshteinDistanceTest(String original, String word2test, double threshold) {
        String originalLowerCase = original.toLowerCase();
        String word2TestLowerCase = word2test.toLowerCase();

        int areWordsSimilarMinLength = CommonTextToolsConfig.LEVENSHTEIN_MIN_LENGTH;
        int areWordsSimilarMaxLdist = CommonTextToolsConfig.LEVENSHTEIN_MAX_DISTANCE;
        int maxLevenshteinDistance = (int) Math.min(areWordsSimilarMaxLdist, threshold * Math.min(original.length(), word2test.length()));

        int levenshteinDistance = levenShteinDistance.apply(originalLowerCase, word2TestLowerCase);

        if (original.length() <= areWordsSimilarMinLength) {
            var wordsHaveContainmentRelation = word2TestLowerCase.contains(originalLowerCase) || originalLowerCase.contains(word2TestLowerCase);
            if (levenshteinDistance <= areWordsSimilarMaxLdist && wordsHaveContainmentRelation) {
                return true;
            }
        } else if (levenshteinDistance <= maxLevenshteinDistance) {
            return true;
        }
        return false;
    }

    /**
     * Checks the similarity of a list with test strings to a list of "original" strings. In this method all test
     * strings are compared to all originals. For this the method uses the
     * {@link #areWordsSimilar(String, String, Double)} with a given threshold. All matches are counted. If the
     * proportion of similarities between the lists is greater than the given threshold the method returns true.
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

        return counter / Math.max(originals.size(), words2test.size()) >= minProportion;
    }

    /**
     * Checks the similarity of a list, containing test strings, and a list of originals. This check is not
     * bidirectional! This method uses the {@link #areWordsOfListsSimilar(List, List, double)} with a given threshold.
     *
     * @param originals  list of original strings
     * @param words2test list of test strings
     * @return true if the list are similar, false if not
     */
    public static boolean areWordsOfListsSimilar(ImmutableList<String> originals, ImmutableList<String> words2test) {
        return areWordsOfListsSimilar(originals, words2test, CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD);
    }

    /**
     * Extracts mappings out of a list, containing mappings, by similarity to an instance. This check is not
     * bidirectional! This method uses the {@link #areWordsOfListsSimilar(List, List)} with the ref as original and the
     * reference of the mappings as test strings.
     *
     * @param ref          the given ref to search for
     * @param nounMappings the mappings to filter
     * @return list of mappings which are similar to the given ref.
     */
    private static ImmutableList<INounMapping> getAllSimilarNMappingsByReference(String ref, ImmutableList<INounMapping> nounMappings) {
        return nounMappings.select(n -> SimilarityUtils.areWordsSimilar(n.getReference(), ref));
    }

    /**
     * Extracts most likely matches of a list of recommended instances by similarity to a given instance. For this, the
     * method uses an increasing minimal proportional threshold with the method
     * {@link #areWordsOfListsSimilar(List, List, double)}. If all lists are similar to the given instance by a
     * threshold of 1-increase value the while loop can be left. If the while loop ends with more than one possibility
     * or all remaining lists are sorted out in the same run, all are returned. Elsewhere only the remaining recommended
     * instance is returned within the list.
     *
     * @param instance             instance to use as original for compare
     * @param recommendedInstances recommended instances to check for similarity
     * @return a list of the most similar recommended instances (to the instance names)
     */
    public static ImmutableList<IRecommendedInstance> getMostRecommendedInstancesToInstanceByReferences(IModelInstance instance,
            ImmutableList<IRecommendedInstance> recommendedInstances) {
        ImmutableList<String> instanceNames = instance.getNameParts();
        double similarity = CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD;
        ImmutableList<IRecommendedInstance> selection = recommendedInstances.select(ri -> checkRecommendedInstanceForSelection(instance, ri, similarity));

        double getMostRecommendedIByRefMinProportion = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION;
        double getMostRecommendedIByRefIncrease = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_INCREASE;

        MutableList<IRecommendedInstance> whileSelection = Lists.mutable.withAll(selection);
        var allListsSimilar = 0;

        while (whileSelection.size() > 1 && getMostRecommendedIByRefMinProportion <= 1) {
            selection = Lists.immutable.withAll(whileSelection);
            getMostRecommendedIByRefMinProportion += getMostRecommendedIByRefIncrease;
            MutableList<IRecommendedInstance> risToRemove = Lists.mutable.empty();
            for (IRecommendedInstance ri : whileSelection) {
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
            } else {
                allListsSimilar = 0;
            }
        }
        if (whileSelection.isEmpty()) {
            return selection;
        }
        return whileSelection.toImmutable();

    }

    private static boolean checkRecommendedInstanceWordSimilarityToInstance(IModelInstance instance, IRecommendedInstance ri) {
        ImmutableList<String> instanceNames = instance.getNameParts();
        for (var sf : ri.getNameMappings().flatCollect(INounMapping::getSurfaceForms)) {
            var splitSF = CommonUtilities.splitCases(String.join(" ", CommonUtilities.splitAtSeparators(sf)));
            if (areWordsSimilar(String.join(" ", instanceNames), splitSF)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkRecommendedInstanceForSelection(IModelInstance instance, IRecommendedInstance ri, double similarity) {
        ImmutableList<String> instanceNames = instance.getNameParts();
        ImmutableList<String> longestNameSplit = Lists.immutable.of(CommonUtilities.splitCases(instance.getFullName()).split(" "));
        ImmutableList<String> recommendedInstanceNameList = Lists.immutable.with(ri.getName());
        if (SimilarityUtils.areWordsSimilar(instance.getFullName(), ri.getName())
                || SimilarityUtils.areWordsOfListsSimilar(instanceNames, recommendedInstanceNameList, similarity)
                || SimilarityUtils.areWordsOfListsSimilar(longestNameSplit, recommendedInstanceNameList, similarity)) {
            return true;
        }
        for (var nounMapping : ri.getNameMappings()) {
            for (var surfaceForm : nounMapping.getSurfaceForms()) {
                var splitSurfaceForm = CommonUtilities.splitCases(surfaceForm);
                var surfaceFormWords = CommonUtilities.splitAtSeparators(splitSurfaceForm);
                if (SimilarityUtils.areWordsOfListsSimilar(instanceNames, surfaceFormWords, similarity)
                        || SimilarityUtils.areWordsOfListsSimilar(longestNameSplit, surfaceFormWords, similarity)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Selects most similar mappings by a given ref. This method compares the given reference, with the references of
     * the given mappings. This method works almost similar to
     * {@link #getMostRecommendedInstancesToInstanceByReferences(Instance, List)}.
     *
     * @param ref          the given reference
     * @param nounMappings the noun mappings to filter
     * @return the most similar noun mapping(s)
     */
    public static ImmutableList<INounMapping> getMostLikelyNounMappingsByReference(String ref, ImmutableList<INounMapping> nounMappings) {

        double threshold = CommonTextToolsConfig.GET_MOST_LIKELY_MP_BY_REFERENCE_THRESHOLD;
        ImmutableList<INounMapping> selection = Lists.immutable.withAll(SimilarityUtils.getAllSimilarNMappingsByReference(ref, nounMappings));
        ImmutableList<INounMapping> whileSelection = Lists.immutable.withAll(selection);

        while (whileSelection.size() > 1 && threshold < 1) {
            selection = Lists.immutable.withAll(whileSelection);
            threshold += CommonTextToolsConfig.GET_MOST_LIKELY_MP_BY_REFERENCE_INCREASE;
            final double currentThreshold = threshold;
            whileSelection = whileSelection.select(nnm -> areWordsSimilar(ref, nnm.getReference(), currentThreshold));

        }
        if (whileSelection.isEmpty()) {
            return selection;
        }

        return whileSelection;

    }

}
