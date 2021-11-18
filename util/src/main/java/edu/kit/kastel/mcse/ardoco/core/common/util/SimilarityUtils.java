package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.util.List;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;

/**
 * This class is a utility class.
 *
 * @author Sophie
 *
 */
public final class SimilarityUtils {

    private static LevenshteinDistance ldistance = new LevenshteinDistance();

    private SimilarityUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Checks the similarity of a test string to an original string. This check is not bidirectional! The test string
     * has to cover the original. If the original is short (e.g. <3) the similarity is harder to reach. Elsewhere, the
     * similarity very depends on the coverage of both strings. The similarity allows little errors within the string.
     * For the comparison the longest common substring and the levenshtein distance are used.
     *
     * @param original  original string
     * @param word2test test string to match the original
     * @param threshold threshold for granularity of similarity
     * @return true, if the test string is similar to the original; false if not.
     */
    public static boolean areWordsSimilar(String original, String word2test, Double threshold) {
        if (original == null || word2test == null) {
            return false;
        }
        String originalLowerCase = original.toLowerCase();
        String word2TestLowerCase = word2test.toLowerCase();
        if (originalLowerCase.split(" ").length != word2TestLowerCase.split(" ").length) {
            return false;
        }
        int areWordsSimilarMinLength = CommonTextToolsConfig.ARE_WORDS_SIMILAR_MIN_LENGTH;
        int areWordsSimilarMaxLdist = CommonTextToolsConfig.ARE_WORDS_SIMILAR_MAX_L_DIST;
        int ldist = ldistance.apply(originalLowerCase, word2TestLowerCase);
        int lcscount = getLongestCommonSubstring(originalLowerCase, word2TestLowerCase);
        if (original.length() <= areWordsSimilarMinLength) {
            if (ldist <= areWordsSimilarMaxLdist && lcscount == original.length()) {
                return true;
            }
        } else if (ldist <= areWordsSimilarMaxLdist || lcscount >= (int) (original.length() * threshold)) {
            return true;
        }

        return false;
    }

    /**
     * Checks the similarity of a test string to an original string. This check is not bidirectional! This method uses
     * the {@link #areWordsSimilar(String, String, Double)} with a given threshold.
     *
     * @param original  original string
     * @param word2test test string to match the original
     * @return true, if the test string is similar to the original; false if not.
     */
    public static boolean areWordsSimilar(String original, String word2test) {
        return areWordsSimilar(original, word2test, CommonTextToolsConfig.ARE_WORDS_SIMILAR_DEFAULT_THRESHOLD);
    }

    /**
     * Checks the similarity of a list, containing test strings, and a list of originals. This check is not
     * bidirectional! In this method all test strings are compared to all originals. For this the method uses the
     * {@link #areWordsSimilar(String, String, Double)} with a given threshold. All matches are counted. If the
     * proportion of similarities between the lists is greater than the given threshold the method returns true.
     *
     * @param originals     list of original strings
     * @param words2test    list of test strings
     * @param minProportion threshold for proportional similarity between the lists
     * @return true if the list are similar, false if not
     */
    public static boolean areWordsOfListsSimilar(ImmutableList<String> originals, ImmutableList<String> words2test, double minProportion) {

        if (areWordsSimilar(String.join(" ", originals), String.join(" ", words2test), minProportion)) {
            return true;
        }

        var counter = 0;
        for (String o : originals) {
            for (String wd : words2test) {
                if (areWordsSimilar(o, wd, CommonTextToolsConfig.ARE_WORDS_OF_LISTS_SIMILAR_WORD_SIMILARITY_THRESHOLD)) {
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
        return areWordsOfListsSimilar(originals, words2test, CommonTextToolsConfig.ARE_WORDS_OF_LISTS_SIMILAR_DEFAULT_THRESHOLD);
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
     * threshold of 1-increase value the while loop can be leaved. If the while loop ends with more than one possibility
     * or all remaining lists are sorted out in the same run, all are returned. Elsewhere only the remaining recommended
     * instance is returned within the list.
     *
     * @param instance             instance to use as original for compare
     * @param recommendedInstances recommended instances to check for similarity
     * @return a list of the most similar recommended instances (to the instance names)
     */
    public static ImmutableList<IRecommendedInstance> getMostRecommendedInstancesToInstanceByReferences(IModelInstance instance,
            ImmutableList<IRecommendedInstance> recommendedInstances) {
        ImmutableList<String> instanceNames = instance.getNames();
        ImmutableList<IRecommendedInstance> selection = recommendedInstances.select(//
                ri -> (SimilarityUtils.areWordsOfListsSimilar(instanceNames, Lists.immutable.with(ri.getName()))
                        || SimilarityUtils.areWordsSimilar(instance.getLongestName(), ri.getName())));

        double getMostRecommendedIByRefMinProportion = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION;
        double getMostRecommendedIByRefIncrease = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_INCREASE;

        MutableList<IRecommendedInstance> whileSelection = Lists.mutable.withAll(selection);
        var allListsSimilar = 0;

        while (whileSelection.size() > 1 && getMostRecommendedIByRefMinProportion <= 1) {
            selection = Lists.immutable.withAll(whileSelection);
            getMostRecommendedIByRefMinProportion += getMostRecommendedIByRefIncrease;
            MutableList<IRecommendedInstance> risToRemove = Lists.mutable.empty();
            for (IRecommendedInstance ri : whileSelection) {

                if (areWordsSimilar(String.join(" ", instanceNames), String.join(" ", ri.getName()), 1 - getMostRecommendedIByRefIncrease)) {
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

    /**
     * Selects most similar mappings by a given ref. This method compares the given reference, with the references of
     * the given mappings. This method works almost similar to
     * {@link #getMostRecommendedInstancesToInstanceByReferences(Instance, List)}.
     *
     * @param ref          the given reference
     * @param nounMappings the noun mappings to filter
     * @return the most similar noun mapping(s)
     */
    public static ImmutableList<INounMapping> getMostLikelyNMappingsByReference(String ref, ImmutableList<INounMapping> nounMappings) {

        double threshold = CommonTextToolsConfig.GET_MOST_LIKELY_MP_BY_REFERENCE_THRESHOLD;
        ImmutableList<INounMapping> selection = Lists.immutable.withAll(SimilarityUtils.getAllSimilarNMappingsByReference(ref, nounMappings));
        ImmutableList<INounMapping> whileSelection = Lists.immutable.withAll(selection);

        while (whileSelection.size() > 1 && threshold < 1) {
            selection = Lists.immutable.withAll(whileSelection);
            threshold += CommonTextToolsConfig.GET_MOST_LIKELY_MP_BY_REFERENCE_INCREASE;
            final double wTh = threshold;
            whileSelection = whileSelection.select(nnm -> SimilarityUtils.areWordsSimilar(ref, nnm.getReference(), wTh));

        }
        if (whileSelection.isEmpty()) {
            return selection;
        }

        return whileSelection;

    }

    /**
     * Counts the longest common substring of two strings. Source:
     * https://www.programcreek.com/2015/04/longest-common-substring-java/
     *
     * @param a first String
     * @param b second String
     * @return size of the longest common substring
     */
    private static int getLongestCommonSubstring(String a, String b) {
        int m = a.length();
        int n = b.length();

        var max = 0;

        var dp = new int[m][n];

        for (var i = 0; i < m; i++) {
            for (var j = 0; j < n; j++) {
                if (a.charAt(i) == b.charAt(j)) {
                    compareAndSetLengthOfCommonSubstringAt(dp, i, j);

                    if (max < dp[i][j]) {
                        max = dp[i][j];
                    }
                }

            }
        }

        return max;
    }

    private static void compareAndSetLengthOfCommonSubstringAt(int[][] dp, int i, int j) {
        if (i == 0 || j == 0) {
            dp[i][j] = 1;
        } else {
            dp[i][j] = dp[i - 1][j - 1] + 1;
        }
    }

}
