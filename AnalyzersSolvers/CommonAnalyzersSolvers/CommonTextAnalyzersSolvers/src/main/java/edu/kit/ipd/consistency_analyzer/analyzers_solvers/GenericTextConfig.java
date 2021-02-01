package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class GenericTextConfig {

    private GenericTextConfig() {
        throw new IllegalAccessError();
    }

    private static final SystemParameters CONFIG = loadParameters("/configs/TextAnalyzerSolverConfig.properties");

    public static final List<String> TEXT_EXTRACTORS = CONFIG.getPropertyAsList("Extractors");

    // ArticleTypeNameAnalyzer
    /**
     * The probability of the article type name analyzer.
     */
    public static final double ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY = CONFIG.getPropertyAsDouble("ArticleTypeNameAnalyzer_Probability");

    // InDepArcsAnalyzer
    /**
     * The probability of the in dep arcs analyzer.
     */
    public static final double IN_DEP_ARCS_ANALYZER_PROBABILITY = CONFIG.getPropertyAsDouble("InDepArcsAnalyzer_Probability");

    // MultiplePartSolver
    /**
     * The probability of the multiple part solver.
     */
    public static final double MULTIPLE_PART_SOLVER_PROBABILITY = CONFIG.getPropertyAsDouble("MultiplePartSolver_Probability");

    // NounAnalyzer
    /**
     * The probability of the noun analyzer.
     */
    public static final double NOUN_ANALYZER_PROBABILITY = CONFIG.getPropertyAsDouble("NounAnalyzer_Probability");

    // OutDepArcsAnalyzer
    /**
     * The probability of the out dep arcs analyzer.
     */
    public static final double OUT_DEP_ARCS_ANALYZER_PROBABILITY = CONFIG.getPropertyAsDouble("OutDepArcsAnalyzer_Probability");

    // SeparatedNamesAnalyzer
    /**
     * The probability of the separated names analyzer.
     */
    public static final double SEPARATED_NAMES_ANALYZER_PROBABILITY = CONFIG.getPropertyAsDouble("SeparatedNamesAnalyzer_Probability");

    private static SystemParameters loadParameters(String filePath) {
        return new SystemParameters(filePath, true);
    }
}
