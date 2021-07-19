package edu.kit.kastel.mcse.ardoco.core.textextractor.agents_extractors;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.ResourceAccessor;

/**
 * The Class GenericTextConfig defines the configuration for this stage.
 */
public class GenericTextConfig extends Configuration {

    private static final String SEPARATED_NAMES_ANALYZER_PROBABILITY = "SeparatedNamesAnalyzer_Probability";
    private static final String OUT_DEP_ARCS_ANALYZER_PROBABILITY = "OutDepArcsAnalyzer_Probability";
    private static final String NOUN_ANALYZER_PROBABILITY = "NounAnalyzer_Probability";
    private static final String MULTIPLE_PART_SOLVER_PROBABILITY = "MultiplePartSolver_Probability";
    private static final String IN_DEP_ARCS_ANALYZER_PROBABILITY = "InDepArcsAnalyzer_Probability";
    private static final String ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY = "ArticleTypeNameAnalyzer_Probability";
    private static final String EXTRACTORS = "Extractors";

    /** The DEFAULT_CONFIG. */
    public static final GenericTextConfig DEFAULT_CONFIG = new GenericTextConfig();

    /** The text extractors to be loaded. */
    public final List<String> textExtractors;

    // ArticleTypeNameAnalyzer
    /**
     * The probability of the article type name analyzer.
     */
    public final double articleTypeNameAnalyzerProbability;

    // InDepArcsAnalyzer
    /**
     * The probability of the in dep arcs analyzer.
     */
    public final double inDepArcsAnalyzerProbability;

    // MultiplePartSolver
    /**
     * The probability of the multiple part solver.
     */
    public final double multiplePartSolverProbability;

    // NounAnalyzer
    /**
     * The probability of the noun analyzer.
     */
    public final double nounAnalyzerProbability;

    // OutDepArcsAnalyzer
    /**
     * The probability of the out dep arcs analyzer.
     */
    public final double outDepArcsAnalyzerProbability;

    // SeparatedNamesAnalyzer
    /**
     * The probability of the separated names analyzer.
     */
    public final double separatedNamesAnalyzerProbability;

    private GenericTextConfig() {
        var config = new ResourceAccessor("/configs/TextAnalyzerSolverConfig.properties", true);
        textExtractors = config.getPropertyAsList(EXTRACTORS);
        articleTypeNameAnalyzerProbability = config.getPropertyAsDouble(ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY);
        inDepArcsAnalyzerProbability = config.getPropertyAsDouble(IN_DEP_ARCS_ANALYZER_PROBABILITY);
        multiplePartSolverProbability = config.getPropertyAsDouble(MULTIPLE_PART_SOLVER_PROBABILITY);
        nounAnalyzerProbability = config.getPropertyAsDouble(NOUN_ANALYZER_PROBABILITY);
        outDepArcsAnalyzerProbability = config.getPropertyAsDouble(OUT_DEP_ARCS_ANALYZER_PROBABILITY);
        separatedNamesAnalyzerProbability = config.getPropertyAsDouble(SEPARATED_NAMES_ANALYZER_PROBABILITY);
    }

    /**
     * Instantiates a new generic text config.
     *
     * @param configs the configs
     */
    public GenericTextConfig(Map<String, String> configs) {
        textExtractors = getPropertyAsList(EXTRACTORS, configs);
        articleTypeNameAnalyzerProbability = getPropertyAsDouble(ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY, configs);
        inDepArcsAnalyzerProbability = getPropertyAsDouble(IN_DEP_ARCS_ANALYZER_PROBABILITY, configs);
        multiplePartSolverProbability = getPropertyAsDouble(MULTIPLE_PART_SOLVER_PROBABILITY, configs);
        nounAnalyzerProbability = getPropertyAsDouble(NOUN_ANALYZER_PROBABILITY, configs);
        outDepArcsAnalyzerProbability = getPropertyAsDouble(OUT_DEP_ARCS_ANALYZER_PROBABILITY, configs);
        separatedNamesAnalyzerProbability = getPropertyAsDouble(SEPARATED_NAMES_ANALYZER_PROBABILITY, configs);

    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(//
                EXTRACTORS, String.join(" ", textExtractors), //
                ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY, String.valueOf(articleTypeNameAnalyzerProbability), //
                IN_DEP_ARCS_ANALYZER_PROBABILITY, String.valueOf(inDepArcsAnalyzerProbability), //
                MULTIPLE_PART_SOLVER_PROBABILITY, String.valueOf(multiplePartSolverProbability), //
                NOUN_ANALYZER_PROBABILITY, String.valueOf(nounAnalyzerProbability), //
                OUT_DEP_ARCS_ANALYZER_PROBABILITY, String.valueOf(outDepArcsAnalyzerProbability), //
                SEPARATED_NAMES_ANALYZER_PROBABILITY, String.valueOf(separatedNamesAnalyzerProbability) //
        );
    }

}
