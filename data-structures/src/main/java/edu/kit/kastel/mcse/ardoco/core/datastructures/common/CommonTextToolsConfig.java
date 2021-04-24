package edu.kit.kastel.mcse.ardoco.core.datastructures.common;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class CommonTextToolsConfig {

    private CommonTextToolsConfig() {
        throw new IllegalAccessError();
    }

    private static final SystemParameters CONFIG = loadParameters("/configs/CommonTextToolsConfig.properties");
    // SimilarityUtils:
    /**
     * List of separators used for containing by a word in SimilarityUtils.
     */
    protected static final List<String> SEPARATORS_TO_CONTAIN = CONFIG.getPropertyAsList("separators_ToContain");
    /**
     * List of separators used for splitting a word in SimilarityUtils.
     */
    protected static final List<String> SEPARATORS_TO_SPLIT = CONFIG.getPropertyAsList("separators_ToSplit");

    /**
     * Int for the minimal length of word similarity for methods in SimilarityUtils.
     */
    public static final int ARE_WORDS_SIMILAR_MIN_LENGTH = CONFIG.getPropertyAsInt("areWordsSimilar_MinLength");
    /**
     * Int for the maximal levensthein distance for two words to be similar for methods in SimilarityUtils.
     */
    public static final int ARE_WORDS_SIMILAR_MAX_L_DIST = CONFIG.getPropertyAsInt("areWordsSimilar_MaxLdist");
    /**
     * The default threshold for similarity in methods of SimilarityUtils.
     */
    public static final double ARE_WORDS_SIMILAR_DEFAULT_THRESHOLD = CONFIG.getPropertyAsDouble("areWordsSimilar_DefaultThreshold");

    /**
     * Threshold for the similarity of two words in the similarity function of two lists.
     */
    public static final double ARE_WORDS_OF_LISTS_SIMILAR_WORD_SIMILARITY_THRESHOLD = //
            CONFIG.getPropertyAsDouble("areWordsOfListsSimilar_WordSimilarityThreshold");

    /**
     * Default threshold for the similarity function of two lists.
     */
    public static final double ARE_WORDS_OF_LISTS_SIMILAR_DEFAULT_THRESHOLD = CONFIG.getPropertyAsDouble("areWordsOfListsSimilar_DefaultThreshold");
    /**
     * The minimal propotion of two lists that need to be similar, that both are similar. Used in SimilarityUtils.
     */
    public static final double GET_MOST_RECOMMENDED_I_BY_REF_MIN_PROPORTION = CONFIG.getPropertyAsDouble("getMostRecommendedIByRef_MinProportion");
    /**
     * The increase for the method getMostRecommendedInstancesByReference in SimilarityUtils.
     */
    public static final double GET_MOST_RECOMMENDED_I_BY_REF_INCREASE = CONFIG.getPropertyAsDouble("getMostRecommendedIByRef_Increase");

    /**
     * The threshold for the method getMostLikelyMappingByReference in SimilarityUtils.
     */
    public static final double GET_MOST_LIKELY_MP_BY_REFERENCE_THRESHOLD = CONFIG.getPropertyAsDouble("getMostLikelyMpByReference_Threshold");
    /**
     * The increase for the method getMostLikelyMappingByReference in SimilarityUtils.
     */
    public static final double GET_MOST_LIKELY_MP_BY_REFERENCE_INCREASE = CONFIG.getPropertyAsDouble("getMostLikelyMpBReference_Increase");

    private static SystemParameters loadParameters(String filePath) {
        return new SystemParameters(filePath, true);
    }

}
