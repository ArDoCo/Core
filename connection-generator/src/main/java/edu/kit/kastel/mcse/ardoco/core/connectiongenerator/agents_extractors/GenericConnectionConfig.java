package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class GenericConnectionConfig extends Configuration {

    public static final GenericConnectionConfig DEFAULT_CONFIG = new GenericConnectionConfig();

    public final List<String> connectionExtractors;

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

    /**
     * The probability of the relation connection solver.
     */
    public final double relationConnectionSolverProbability;

    /**
     * The probability of the instance mapping connection solver.
     */
    public final double instanceConnectionSolverProbability;
    /**
     * The probability of the instance mapping connection solver, if the connection does not include the comparison of a
     * type.
     */
    public final double instanceConnectionSolverProbabilityWithoutType;

    private GenericConnectionConfig() {
        SystemParameters config = new SystemParameters("/configs/ConnectionAnalyzerSolverConfig.properties", true);
        connectionExtractors = config.getPropertyAsList("Connection_Extractors");
        extractionDependentOccurrenceAnalyzerProbability = config.getPropertyAsDouble("ExtractionDependentOccurrenceAnalyzer_Probability");
        extractedTermsAnalyzerProbabilityAdjacentNoun = config.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentNoun");
        extractedTermsAnalyzerProbabilityJustName = config.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityJustName");
        extractedTermsAnalyzerProbabilityAdjacentTerm = config.getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentTerm");
        referenceSolverProbability = config.getPropertyAsDouble("ReferenceSolver_Probability");
        referenceSolverProportionalDecrease = config.getPropertyAsDouble("ReferenceSolver_ProportionalDecrease");
        referenceSolverAreNamesSimilarThreshold = config.getPropertyAsDouble("ReferenceSolver_areNamesSimilarThreshold");
        relationConnectionSolverProbability = config.getPropertyAsDouble("RelationConnectionSolver_Probability");
        instanceConnectionSolverProbability = config.getPropertyAsDouble("InstanceConnectionSolver_Probability");
        instanceConnectionSolverProbabilityWithoutType = config.getPropertyAsDouble("InstanceConnectionSolver_ProbabilityWithoutType");
    }

    public GenericConnectionConfig(Map<String, String> configs) {
        connectionExtractors = getPropertyAsList("Connection_Extractors", configs);
        extractionDependentOccurrenceAnalyzerProbability = getPropertyAsDouble("ExtractionDependentOccurrenceAnalyzer_Probability", configs);
        extractedTermsAnalyzerProbabilityAdjacentNoun = getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentNoun", configs);
        extractedTermsAnalyzerProbabilityJustName = getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityJustName", configs);
        extractedTermsAnalyzerProbabilityAdjacentTerm = getPropertyAsDouble("ExtractedTermsAnalyzer_ProbabilityAdjacentTerm", configs);
        referenceSolverProbability = getPropertyAsDouble("ReferenceSolver_Probability", configs);
        referenceSolverProportionalDecrease = getPropertyAsDouble("ReferenceSolver_ProportionalDecrease", configs);
        referenceSolverAreNamesSimilarThreshold = getPropertyAsDouble("ReferenceSolver_areNamesSimilarThreshold", configs);
        relationConnectionSolverProbability = getPropertyAsDouble("RelationConnectionSolver_Probability", configs);
        instanceConnectionSolverProbability = getPropertyAsDouble("InstanceConnectionSolver_Probability", configs);
        instanceConnectionSolverProbabilityWithoutType = getPropertyAsDouble("InstanceConnectionSolver_ProbabilityWithoutType", configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("Connection_Extractors", String.join(" ", connectionExtractors));
        properties.put("ExtractionDependentOccurrenceAnalyzer_Probability", String.valueOf(extractionDependentOccurrenceAnalyzerProbability));
        properties.put("ExtractedTermsAnalyzer_ProbabilityAdjacentNoun", String.valueOf(extractedTermsAnalyzerProbabilityAdjacentNoun));
        properties.put("ExtractedTermsAnalyzer_ProbabilityJustName", String.valueOf(extractedTermsAnalyzerProbabilityJustName));
        properties.put("ExtractedTermsAnalyzer_ProbabilityAdjacentTerm", String.valueOf(extractedTermsAnalyzerProbabilityAdjacentTerm));
        properties.put("ReferenceSolver_Probability", String.valueOf(referenceSolverProbability));
        properties.put("ReferenceSolver_ProportionalDecrease", String.valueOf(referenceSolverProportionalDecrease));
        properties.put("ReferenceSolver_areNamesSimilarThreshold", String.valueOf(referenceSolverAreNamesSimilarThreshold));
        properties.put("RelationConnectionSolver_Probability", String.valueOf(relationConnectionSolverProbability));
        properties.put("InstanceConnectionSolver_Probability", String.valueOf(instanceConnectionSolverProbability));
        properties.put("InstanceConnectionSolver_ProbabilityWithoutType", String.valueOf(instanceConnectionSolverProbabilityWithoutType));

        return properties;
    }

}
