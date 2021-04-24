package edu.kit.kastel.mcse.ardoco.core.datastructures.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;

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
	 * Checks the similarity of a test string to an original string. This check is
	 * not bidirectional! The test string has to cover the original. If the original
	 * is short (e.g. <3) the similarity is harder to reach. Elsewhere, the
	 * similarity very depends on the coverage of both strings. The similarity
	 * allows little errors within the string. For the comparison the longest common
	 * substring and the levenshtein distance are used.
	 *
	 * @param original  original string
	 * @param word2test test string to match the original
	 * @param threshold threshold for granularity of similarity
	 * @return true, if the test string is similar to the original; false if not.
	 */
	public static boolean areWordsSimilar(String original, String word2test, Double threshold) {
		if (original.toLowerCase().split(" ").length != word2test.toLowerCase().split(" ").length) {
			return false;
		}
		int areWordsSimilarMinLength = CommonTextToolsConfig.ARE_WORDS_SIMILAR_MIN_LENGTH;
		int areWordsSimilarMaxLdist = CommonTextToolsConfig.ARE_WORDS_SIMILAR_MAX_L_DIST;
		int ldist = ldistance.apply(original.toLowerCase(), word2test.toLowerCase());
		int lcscount = getLongestCommonSubstring(original.toLowerCase(), word2test.toLowerCase());
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
	 * Checks the similarity of a test string to an original string. This check is
	 * not bidirectional! This method uses the
	 * {@link #areWordsSimilar(String, String, Double)} with a given threshold.
	 *
	 * @param original  original string
	 * @param word2test test string to match the original
	 * @return true, if the test string is similar to the original; false if not.
	 */
	public static boolean areWordsSimilar(String original, String word2test) {
		return areWordsSimilar(original, word2test, CommonTextToolsConfig.ARE_WORDS_SIMILAR_DEFAULT_THRESHOLD);
	}

	/**
	 * Checks the similarity of a list, containing test strings, and a list of
	 * originals. This check is not bidirectional! In this method all test strings
	 * are compared to all originals. For this the method uses the
	 * {@link #areWordsSimilar(String, String, Double)} with a given threshold. All
	 * matches are counted. If the proportion of similarities between the lists is
	 * greater than the given threshold the method returns true.
	 *
	 * @param originals     list of original strings
	 * @param words2test    list of test strings
	 * @param minProportion threshold for proportional similarity between the lists
	 * @return true if the list are similar, false if not
	 */
	public static boolean areWordsOfListsSimilar(List<String> originals, List<String> words2test, double minProportion) {

		if (areWordsSimilar(String.join(" ", originals), String.join(" ", words2test), minProportion)) {
			return true;
		}

		int counter = 0;
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
	 * Checks the similarity of a list, containing test strings, and a list of
	 * originals. This check is not bidirectional! This method uses the
	 * {@link #areWordsOfListsSimilar(List, List, double)} with a given threshold.
	 *
	 * @param originals  list of original strings
	 * @param words2test list of test strings
	 * @return true if the list are similar, false if not
	 */
	public static boolean areWordsOfListsSimilar(List<String> originals, List<String> words2test) {
		return areWordsOfListsSimilar(originals, words2test, CommonTextToolsConfig.ARE_WORDS_OF_LISTS_SIMILAR_DEFAULT_THRESHOLD);
	}

	/**
	 * Extracts mappings out of a list, containing mappings, by similarity to an
	 * instance. This check is not bidirectional! This method uses the
	 * {@link #areWordsOfListsSimilar(List, List)} with the names of the instance as
	 * original and the reference of the mappings as test strings.
	 *
	 * @param instance the model instance
	 * @param mapping  list of mappings
	 * @return list of mappings which are similar to the instance.
	 */
	public static List<INounMapping> getAllSimilarNMappingToInstanceByReferences(IInstance instance, List<INounMapping> mapping) {
		List<String> instanceNames = instance.getNames();
		return mapping.stream().filter(n -> SimilarityUtils.areWordsOfListsSimilar(instanceNames, List.of(n.getReference()))).collect(Collectors.toList());

	}

	/**
	 * Extracts mappings out of a list, containing mappings, by similarity to an
	 * instance. This check is not bidirectional! This method uses the
	 * {@link #areWordsOfListsSimilar(List, List)} with the ref as original and the
	 * reference of the mappings as test strings.
	 *
	 * @param ref           the given ref to search for
	 * @param INounMappings the mappings to filter
	 * @return list of mappings which are similar to the given ref.
	 */
	public static List<INounMapping> getAllSimilarNMappingsByReference(String ref, List<INounMapping> nounMappings) {

		return nounMappings.stream().filter(n -> SimilarityUtils.areWordsSimilar(n.getReference(), ref)).collect(Collectors.toList());

	}

	/**
	 * Extracts most likely matches of a list of recommended instances by similarity
	 * to a given instance. For this, the method uses an increasing minimal
	 * proportional threshold with the method
	 * {@link #areWordsOfListsSimilar(List, List, double)}. If all lists are similar
	 * to the given instance by a threshold of 1-increase value the while loop can
	 * be leaved. If the while loop ends with more than one possibility or all
	 * remaining lists are sorted out in the same run, all are returned. Elsewhere
	 * only the remaining recommended instance is returned within the list.
	 *
	 * @param instance             instance to use as original for compare
	 * @param recommendedInstances recommended instances to check for similarity
	 * @return a list of the most similar recommended instances (to the instance
	 *         names)
	 */
	public static List<IRecommendedInstance> getMostRecommendedInstancesToInstanceByReferences(IInstance instance,
			List<IRecommendedInstance> recommendedInstances) {
		List<String> instanceNames = instance.getNames();
		List<IRecommendedInstance> selection = recommendedInstances.stream().filter(//
				ri -> (SimilarityUtils.areWordsOfListsSimilar(instanceNames, List.of(ri.getName()))
						|| SimilarityUtils.areWordsSimilar(instance.getLongestName(), ri.getName())))
				.collect(Collectors.toList());

		double getMostRecommendedIByRefMinProportion = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION;
		double getMostRecommendedIByRefIncrease = CommonTextToolsConfig.GET_MOST_RECOMMENDED_I_BY_REF_INCREASE;

		List<IRecommendedInstance> whileSelection = new ArrayList<>(selection);
		int allListsSimilar = 0;

		while (whileSelection.size() > 1 && getMostRecommendedIByRefMinProportion <= 1) {
			selection = new ArrayList<>(whileSelection);
			getMostRecommendedIByRefMinProportion += getMostRecommendedIByRefIncrease;
			List<IRecommendedInstance> risToRemove = new ArrayList<>();
			for (IRecommendedInstance ri : whileSelection) {

				if (areWordsSimilar(String.join(" ", instanceNames), String.join(" ", ri.getName()), 1 - getMostRecommendedIByRefIncrease)) {
					allListsSimilar++;
				}

				if (!SimilarityUtils.areWordsOfListsSimilar(instanceNames, List.of(ri.getName()), getMostRecommendedIByRefMinProportion)) {
					risToRemove.add(ri);
				}
			}
			whileSelection.removeAll(risToRemove);
			if (allListsSimilar == whileSelection.size()) {
				return whileSelection;
			} else {
				allListsSimilar = 0;
			}
		}
		if (whileSelection.isEmpty()) {
			return selection;
		}
		return whileSelection;

	}

	/**
	 * Selects most similar mappings by a given ref. This method compares the given
	 * reference, with the references of the given mappings. This method works
	 * almost similar to
	 * {@link #getMostRecommendedInstancesToInstanceByReferences(Instance, List)}.
	 *
	 * @param ref          the given reference
	 * @param nounMappings the noun mappings to filter
	 * @return the most similar noun mapping(s)
	 */
	public static List<INounMapping> getMostLikelyNMappingsByReference(String ref, List<INounMapping> nounMappings) {

		double threshold = CommonTextToolsConfig.GET_MOST_LIKELY_MP_BY_REFERENCE_THRESHOLD;
		List<INounMapping> selection = new ArrayList<>(SimilarityUtils.getAllSimilarNMappingsByReference(ref, nounMappings));
		List<INounMapping> whileSelection = new ArrayList<>(selection);

		while (whileSelection.size() > 1 && threshold < 1) {
			selection = new ArrayList<>(whileSelection);
			threshold += CommonTextToolsConfig.GET_MOST_LIKELY_MP_BY_REFERENCE_INCREASE;
			final double wTh = threshold;
			whileSelection = whileSelection.stream().filter(nnm -> SimilarityUtils.areWordsSimilar(ref, nnm.getReference(), wTh)).collect(Collectors.toList());

		}
		if (whileSelection.isEmpty()) {
			return selection;
		}

		return whileSelection;

	}

	/**
	 * Extracts most similar mappings from a given recommended model instance. This
	 * method uses {@link #getMostLikelyNMappingsByReference(String, List)}.
	 *
	 * @param ri recommended instance under investigation
	 * @return most similar mappings
	 */
	public static List<INounMapping> getMostLikelyNNMappingsByName(IRecommendedInstance ri) {

		return getMostLikelyNMappingsByReference(ri.getName(), ri.getNameMappings());

	}

	/**
	 * Counts the longest common substring of two strings. Source:
	 * https://www.programcreek.com/2015/04/longest-common-substring-java/
	 *
	 * @param a first String
	 * @param b second String
	 * @return size of the longest common substring
	 */
	public static int getLongestCommonSubstring(String a, String b) {
		int m = a.length();
		int n = b.length();

		int max = 0;

		int[][] dp = new int[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
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

	/**
	 * Replaces all defined separators in a given string with a whitespace and
	 * returns the resulting string.
	 *
	 * @param reference given string
	 * @return reference with whitespaces instead of separators
	 */
	public static List<String> splitAtSeparators(String reference) {
		String ref = reference;
		for (String sep : CommonTextToolsConfig.SEPARATORS_TO_SPLIT) {
			ref = ref.replaceAll(sep, " ");
		}
		return new ArrayList<>(List.of(ref.split(" ")));
	}

	/**
	 * Checks if a string contains any separators
	 *
	 * @param reference string to check
	 * @return true, if a separator is contained or false, if not
	 */
	public static boolean containsSeparator(String reference) {
		for (String sep : CommonTextToolsConfig.SEPARATORS_TO_CONTAIN) {
			if (reference.contains(sep) && !reference.contentEquals(sep)) {
				return true;
			}
		}
		return false;
	}

}
