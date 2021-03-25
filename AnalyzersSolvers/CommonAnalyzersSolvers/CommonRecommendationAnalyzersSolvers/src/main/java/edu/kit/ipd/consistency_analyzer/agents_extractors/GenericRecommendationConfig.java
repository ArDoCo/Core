package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class GenericRecommendationConfig extends Configuration {

    public static final GenericRecommendationConfig DEFAULT_CONFIG = new GenericRecommendationConfig();

    public final List<String> recommendationExtractors;

    // ExtractionDependendOccurrenceAnalyzer
    /**
     * The probability of the extraction dependent occurrence analyzer.
     */
    public final double extractionDependentOccurrenceAnalyzerProbability;

    // ExtractedTermsAnalyzer
    /**
     * The probability for terms with an adjacent noun to be of that type or have that name.
     */
    public final double extractedTermsAnalyzerProbabilityAdjacentNoun;
    /**
     * The probability for terms with no adjacent nouns and therefore without type, to be recommended.
     */
    public final double extractedTermsAnalyzerProbabilityJustName;
    /**
     * The probability term combinations are recommended with.
     */
    public final double extractedTermsAnalyzerProbabilityAdjacentTerm;

    // NameTypeAnalyzer
    /**
     * The probability of the name type analyzer.
     */
    public final double nameTypeAnalyzerProbability;

    // ReferenceSolver
    /**
     * The probability of the reference solver.
     */
    public final double referenceSolverProbability;
    /**
     * The decrease of the reference solver.
     */
    public final double referenceSolverProportionalDecrease;

    /**
     * The threshold for words similarities in the reference solver.
     */
    public final double referenceSolverAreNamesSimilarThreshold;

    // SeparatedRelationSolver
    /**
     * The probability of the separated relations solver.
     */
    public final double separatedRelationSolverProbility;

    public GenericRecommendationConfig() {
        SystemParameters config = new SystemParameters("/configs/RecommendationAnalyzerSolverConfig.properties", true);
        recommendationExtractors = config.getPropertyAsList("Recommendation_Extractors");
        extractionDependentOccurrenceAnalyzerProbability = config.getPropertyAsDouble("ExtractionDependentOccurrenceAnalyzer_Probability");
        extractedTermsAnalyzerProbabilityAdjacentNoun = config.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentNoun");
        extractedTermsAnalyzerProbabilityJustName = config.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityJustName");
        extractedTermsAnalyzerProbabilityAdjacentTerm = config.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentTerm");
        nameTypeAnalyzerProbability = config.getPropertyAsDouble("NameTypeAnalyzer_Probability");
        referenceSolverProbability = config.getPropertyAsDouble("ReferenceSolver_Probability");
        referenceSolverProportionalDecrease = config.getPropertyAsDouble("ReferenceSolver_ProportionalDecrease");
        referenceSolverAreNamesSimilarThreshold = config.getPropertyAsDouble("ReferenceSolver_areNamesSimilarThreshold");
        separatedRelationSolverProbility = config.getPropertyAsDouble("SeparatedRelationsSolver_Probability");
    }

    public GenericRecommendationConfig(List<String> recommendationExtractors, double extractionDependentOccurrenceAnalyzerProbability,
            double extractedTermsAnalyzerProbabilityAdjacentNoun, double extractedTermsAnalyzerProbabilityJustName,
            double extractedTermsAnalyzerProbabilityAdjacentTerm, double nameTypeAnalyzerProbability, double referenceSolverProbability,
            double referenceSolverProportionalDecrease, double referenceSolverAreNamesSimilarThreshold, double separatedRelationSolverProbility) {
        this.recommendationExtractors = recommendationExtractors;
        this.extractionDependentOccurrenceAnalyzerProbability = extractionDependentOccurrenceAnalyzerProbability;
        this.extractedTermsAnalyzerProbabilityAdjacentNoun = extractedTermsAnalyzerProbabilityAdjacentNoun;
        this.extractedTermsAnalyzerProbabilityJustName = extractedTermsAnalyzerProbabilityJustName;
        this.extractedTermsAnalyzerProbabilityAdjacentTerm = extractedTermsAnalyzerProbabilityAdjacentTerm;
        this.nameTypeAnalyzerProbability = nameTypeAnalyzerProbability;
        this.referenceSolverProbability = referenceSolverProbability;
        this.referenceSolverProportionalDecrease = referenceSolverProportionalDecrease;
        this.referenceSolverAreNamesSimilarThreshold = referenceSolverAreNamesSimilarThreshold;
        this.separatedRelationSolverProbility = separatedRelationSolverProbility;
    }

}
