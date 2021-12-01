/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;

/**
 * The configuration for the agents and extractors of the connection generator.
 *
 * @author Dominik Fuchss
 *
 */
public class GenericConnectionConfig extends Configuration {

    private static final String INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE = "InstanceConnectionSolver_ProbabilityWithoutType";
    private static final String INSTANCE_CONNECTION_SOLVER_PROBABILITY = "InstanceConnectionSolver_Probability";
    private static final String RELATION_CONNECTION_SOLVER_PROBABILITY = "RelationConnectionSolver_Probability";
    private static final String REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD = "ReferenceSolver_areNamesSimilarThreshold";
    private static final String REFERENCE_SOLVER_PROPORTIONAL_DECREASE = "ReferenceSolver_ProportionalDecrease";
    private static final String REFERENCE_SOLVER_PROBABILITY = "ReferenceSolver_Probability";
    private static final String EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER_PROBABILITY = "ExtractionDependentOccurrenceAnalyzer_Probability";
    private static final String CONNECTION_EXTRACTORS = "Connection_Extractors";
    private static final String NAME_TYPE_ANALYZER_PROBABILITY = "NameTypeAnalyzer_Probability";

    /**
     * The default configuration to use.
     */
    public static final GenericConnectionConfig DEFAULT_CONFIG = new GenericConnectionConfig();

    /**
     * All extractor names of extractors to be used.
     */
    public final ImmutableList<String> connectionExtractors;

    // NameTypeExtractor
    public final double nameTypeAnalyzerProbability;

    // ExtractionDependendOccurrenceAnalyzer
    /**
     * The probability of the extraction dependent occurrence analyzer.
     */
    public final double extractionDependentOccurrenceAnalyzerProbability;

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
        var config = new ResourceAccessor("/configs/ConnectionAnalyzerSolverConfig.properties", true);
        connectionExtractors = config.getPropertyAsList(CONNECTION_EXTRACTORS);
        nameTypeAnalyzerProbability = config.getPropertyAsDouble(NAME_TYPE_ANALYZER_PROBABILITY);
        extractionDependentOccurrenceAnalyzerProbability = config.getPropertyAsDouble(EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER_PROBABILITY);
        referenceSolverProbability = config.getPropertyAsDouble(REFERENCE_SOLVER_PROBABILITY);
        referenceSolverProportionalDecrease = config.getPropertyAsDouble(REFERENCE_SOLVER_PROPORTIONAL_DECREASE);
        referenceSolverAreNamesSimilarThreshold = config.getPropertyAsDouble(REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD);
        relationConnectionSolverProbability = config.getPropertyAsDouble(RELATION_CONNECTION_SOLVER_PROBABILITY);
        instanceConnectionSolverProbability = config.getPropertyAsDouble(INSTANCE_CONNECTION_SOLVER_PROBABILITY);
        instanceConnectionSolverProbabilityWithoutType = config.getPropertyAsDouble(INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE);
    }

    /**
     * Create the configuration based on the default config values.
     *
     * @param configs contains the keys that have to be overwritten
     */
    public GenericConnectionConfig(Map<String, String> configs) {
        connectionExtractors = getPropertyAsList(CONNECTION_EXTRACTORS, configs);
        extractionDependentOccurrenceAnalyzerProbability = getPropertyAsDouble(EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER_PROBABILITY, configs);
        nameTypeAnalyzerProbability = getPropertyAsDouble(NAME_TYPE_ANALYZER_PROBABILITY, configs);
        referenceSolverProbability = getPropertyAsDouble(REFERENCE_SOLVER_PROBABILITY, configs);
        referenceSolverProportionalDecrease = getPropertyAsDouble(REFERENCE_SOLVER_PROPORTIONAL_DECREASE, configs);
        referenceSolverAreNamesSimilarThreshold = getPropertyAsDouble(REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD, configs);
        relationConnectionSolverProbability = getPropertyAsDouble(RELATION_CONNECTION_SOLVER_PROBABILITY, configs);
        instanceConnectionSolverProbability = getPropertyAsDouble(INSTANCE_CONNECTION_SOLVER_PROBABILITY, configs);
        instanceConnectionSolverProbabilityWithoutType = getPropertyAsDouble(INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE, configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(CONNECTION_EXTRACTORS, String.join(" ", connectionExtractors));
        properties.put(NAME_TYPE_ANALYZER_PROBABILITY, String.valueOf(nameTypeAnalyzerProbability));
        properties.put(EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER_PROBABILITY, String.valueOf(extractionDependentOccurrenceAnalyzerProbability));
        properties.put(REFERENCE_SOLVER_PROBABILITY, String.valueOf(referenceSolverProbability));
        properties.put(REFERENCE_SOLVER_PROPORTIONAL_DECREASE, String.valueOf(referenceSolverProportionalDecrease));
        properties.put(REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD, String.valueOf(referenceSolverAreNamesSimilarThreshold));
        properties.put(RELATION_CONNECTION_SOLVER_PROBABILITY, String.valueOf(relationConnectionSolverProbability));
        properties.put(INSTANCE_CONNECTION_SOLVER_PROBABILITY, String.valueOf(instanceConnectionSolverProbability));
        properties.put(INSTANCE_CONNECTION_SOLVER_PROBABILITY_WITHOUT_TYPE, String.valueOf(instanceConnectionSolverProbabilityWithoutType));

        return properties;
    }

}
