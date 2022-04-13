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
     * Decides whether the levenshtein similarity measure should be used.
     */
    public static final boolean LEVENSHTEIN_ENABLED = CONFIG.isPropertyEnabled("levenshtein_Enabled");
    /**
     * Int for the minimal length of word similarity for methods in SimilarityUtils.
     */
    public static final int LEVENSHTEIN_MIN_LENGTH = CONFIG.getPropertyAsInt("levenshtein_MinLength");
    /**
     * Int for the maximal levensthein distance for two words to be similar for methods in SimilarityUtils.
     */
    public static final int LEVENSHTEIN_MAX_DISTANCE = CONFIG.getPropertyAsInt("levenshtein_MaxDistance");

    /**
     * Decides whether the JaroWinkler similarity measure should be used.
     */
    public static final boolean JAROWINKLER_ENABLED = CONFIG.isPropertyEnabled("jaroWinkler_Enabled");
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

    /**
     * Decides whether the NGram similarity measure should be used.
     */
    public static final boolean NGRAM_ENABLED = CONFIG.isPropertyEnabled("ngram_Enabled");
    /**
     * The length of ngrams for the N-gram word similarity measure.
     */
    public static final int NGRAM_MEASURE_NGRAM_LENGTH = CONFIG.getPropertyAsInt("ngram_NgramLength");
    /**
     * The threshold for the ngram similarity measure above which words are considered similar.
     */
    public static final double NGRAM_SIMILARITY_THRESHOLD = CONFIG.getPropertyAsDouble("ngram_SimilarityThreshold");

    /**
     * Decides whether the SEWordSim similarity measure should be used.
     */
    public static final boolean SEWORDSIM_ENABLED = CONFIG.isPropertyEnabled("sewordsim_Enabled");
    /**
     * The threshold for the SEWordSim similarity measure above which words are considered similar.
     */
    public static final double SEWORDSIM_SIMILARITY_THRESHOLD = CONFIG.getPropertyAsDouble("sewordsim_SimilarityThreshold");
    /**
     * The path to the sqlite database file used by the SEWordSim word similarity measure.
     */
    public static final String SEWORDSIM_DB_FILE_PATH = CONFIG.getProperty("sewordsim_DatabaseFilePath");

    /**
     * Decides whether the fastText similarity measure should be used.
     */
    public static final boolean FASTTEXT_ENABLED = CONFIG.isPropertyEnabled("fastText_Enabled");
    /**
     * The threshold for the fastText similarity measure above which words are considered similar.
     */
    public static final double FASTTEXT_SIMILARITY_THRESHOLD = CONFIG.getPropertyAsDouble("fastText_SimilarityThreshold");
    /**
     * The path to the fastText binary model file used by the fastText word similarity measure.
     */
    public static final String FASTTEXT_MODEL_FILE_PATH = CONFIG.getProperty("fastText_ModelPath");

    /**
     * Decides whether the WordNet similarity measure should be used.
     */
    public static final boolean WORDNET_ENABLED = CONFIG.isPropertyEnabled("wordNet_Enabled");
    /**
     * The path to the WordNet data directory.
     */
    public static final String WORDNET_DATA_DIR_PATH = CONFIG.getProperty("wordNet_DataDirPath");
    /**
     * Decides whether the WordNet relatedness calculators should cache results.
     */
    public static final boolean WORDNET_USE_CACHE = CONFIG.isPropertyEnabled("wordNet_useCache");
    /**
     * Decides whether the Leacock & Chodorow (1998) algorithm should be used for the WordNet similarity measure.
     */
    public static boolean WORDNET_ALGO_LEACOCK_CHODOROW_ENABLED = CONFIG.isPropertyEnabled("wordNet_Algo_LeacockChodorow_Enabled");
    /**
     * The threshold for the Leacock & Chodorow (1998) algorithm above which words are considered similar.
     */
    public static double WORDNET_ALGO_LEACOCK_CHODOROW_THRESHOLD = CONFIG.getPropertyAsDouble("wordNet_Algo_LeacockChodorow_Threshold");
    /**
     * Decides whether the Wu & Palmer (1994) algorithm should be used for the WordNet similarity measure.
     */
    public static boolean WORDNET_ALGO_WU_PALMER_ENABLED = CONFIG.isPropertyEnabled("wordNet_Algo_WuPalmer_Enabled");
    /**
     * The threshold for the Wu & Palmer (1994) algorithm above which words are considered similar.
     */
    public static double WORDNET_ALGO_WU_PALMER_THRESHOLD = CONFIG.getPropertyAsDouble("wordNet_Algo_WuPalmer_Threshold");
    /**
     * Decides whether the Jiang & Conrath (1997) algorithm should be used for the WordNet similarity measure.
     */
    public static boolean WORDNET_ALGO_JIANG_CONRATH_ENABLED = CONFIG.isPropertyEnabled("wordNet_Algo_JiangConrath_Enabled");
    /**
     * The threshold for the Jiang & Conrath (1997) algorithm above which words are considered similar.
     */
    public static double WORDNET_ALGO_JIANG_CONRATH_THRESHOLD = CONFIG.getPropertyAsDouble("wordNet_Algo_JiangConrath_Threshold");
    /**
     * Decides whether the extended Lesk algorithm (Banerjee & Pedersen 2003) should be used for the WordNet similarity
     * measure.
     */
    public static boolean WORDNET_ALGO_EXTENDED_LESK_ENABLED = CONFIG.isPropertyEnabled("wordNet_Algo_ExtendedLesk_Enabled");
    /**
     * The threshold for the extended Lesk algorithm (Banerjee & Pedersen 2003) above which words are considered
     * similar.
     */
    public static double WORDNET_ALGO_EXTENDED_LESK_THRESHOLD = CONFIG.getPropertyAsDouble("wordNet_Algo_ExtendedLesk_Threshold");
    /**
     * Decides whether the Ezzikouri algorithm (Ezzikouri et al. 2019) should be used for the WordNet similarity
     * measure.
     */
    public static boolean WORDNET_ALGO_EZZIKOURI_ENABLED = CONFIG.isPropertyEnabled("wordNet_Algo_Ezzikouri_Enabled");
    /**
     * The threshold for the Ezzikouri algorithm (Ezzikouri et al. 2019) above which words are considered similar.
     */
    public static double WORDNET_ALGO_EZZIKOURI_THRESHOLD = CONFIG.getPropertyAsDouble("wordNet_Algo_Ezzikouri_Threshold");


    /**
     * Decides whether the GloVe similarity measure should be used.
     */
    public static final boolean GLOVE_ENABLED = CONFIG.isPropertyEnabled("glove_Enabled");
    /**
     * The threshold for the GloVe similarity measure above which words are considered similar.
     */
    public static final double GLOVE_SIMILARITY_THRESHOLD = CONFIG.getPropertyAsDouble("glove_SimilarityThreshold");
    /**
     * The path to the sqlite database file used by the GloVe word similarity measure.
     */
    public static final String GLOVE_DB_FILE_PATH = CONFIG.getProperty("glove_DatabaseFilePath");

	/**
	 * Decides whether the Nasari similarity measure should be used.
	 */
	public static final boolean NASARI_ENABLED = CONFIG.isPropertyEnabled("nasari_Enabled");
	/**
	 * The threshold for the Nasari similarity measure above which words are considered similar.
	 */
	public static final double NASARI_SIMILARITY_THRESHOLD = CONFIG.getPropertyAsDouble("nasari_SimilarityThreshold");
	/**
	 * The path to the sqlite database file used by the Nasari word similarity measure.
	 */
	public static final String NASARI_DB_FILE_PATH = CONFIG.getProperty("nasari_DatabaseFilePath");
	/**
	 * The API key used to access information from BabelNet.
	 */
	public static final String BABELNET_API_KEY = CONFIG.getProperty("babelNet_ApiKey");
	/**
	 * The path to the BabelNet cache.
	 */
	public static final String BABELNET_CACHE_FILE_PATH = CONFIG.getProperty("babelNet_CacheFilePath");

    private static ResourceAccessor loadParameters(String filePath) {
        return new ResourceAccessor(filePath, true);
    }

}
