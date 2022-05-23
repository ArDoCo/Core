/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * The Class CommonTextToolsConfig defines the configuration for the common text tools.
 */
public final class CommonTextToolsConfig {

    private CommonTextToolsConfig() {
        throw new IllegalAccessError();
    }

    private static final ResourceAccessor CONFIG = loadParameters("/configs/CommonTextToolsConfig.properties");
    // SimilarityUtils:
    /**
     * List of separators used for containing by a word in SimilarityUtils.
     */
    public static final ImmutableList<String> SEPARATORS_TO_CONTAIN = CONFIG.getPropertyAsList("separators_ToContain");
    /**
     * List of separators used for splitting a word in SimilarityUtils.
     */
    public static final ImmutableList<String> SEPARATORS_TO_SPLIT = CONFIG.getPropertyAsList("separators_ToSplit");

    /**
     * Int for the minimal length of word similarity for methods in SimilarityUtils.
     */
    public static final int LEVENSHTEIN_MIN_LENGTH = CONFIG.getPropertyAsInt("levenshtein_MinLength");
    /**
     * Int for the maximal levensthein distance for two words to be similar for methods in SimilarityUtils.
     */
    public static final int LEVENSHTEIN_MAX_DISTANCE = CONFIG.getPropertyAsInt("levenshtein_MaxDistance");
    /**
     * The default threshold for similarity in methods of SimilarityUtils.
     */
    public static final double JAROWINKLER_SIMILARITY_THRESHOLD = CONFIG.getPropertyAsDouble("jaroWinkler_SimilarityThreshold");

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

    private static ResourceAccessor loadParameters(String filePath) {
        return new ResourceAccessor(filePath, true);
    }

}
