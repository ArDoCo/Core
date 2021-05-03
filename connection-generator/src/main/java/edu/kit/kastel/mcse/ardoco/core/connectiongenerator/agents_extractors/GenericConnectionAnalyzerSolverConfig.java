package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class GenericConnectionAnalyzerSolverConfig extends Configuration {

    public static final GenericConnectionAnalyzerSolverConfig DEFAULT_CONFIG = new GenericConnectionAnalyzerSolverConfig();

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

    private GenericConnectionAnalyzerSolverConfig() {
        SystemParameters config = new SystemParameters("/configs/ConnectionAnalyzerSolverConfig.properties", true);
        relationConnectionSolverProbability = config.getPropertyAsDouble("RelationConnectionSolver_Probability");
        instanceConnectionSolverProbability = config.getPropertyAsDouble("InstanceConnectionSolver_Probability");
        instanceConnectionSolverProbabilityWithoutType = config.getPropertyAsDouble("InstanceConnectionSolver_ProbabilityWithoutType");
    }

    public GenericConnectionAnalyzerSolverConfig(Map<String, String> configs) {
        relationConnectionSolverProbability = getPropertyAsDouble("RelationConnectionSolver_Probability", configs);
        instanceConnectionSolverProbability = getPropertyAsDouble("InstanceConnectionSolver_Probability", configs);
        instanceConnectionSolverProbabilityWithoutType = getPropertyAsDouble("InstanceConnectionSolver_ProbabilityWithoutType", configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(//
                "RelationConnectionSolver_Probability", String.valueOf(relationConnectionSolverProbability), //
                "InstanceConnectionSolver_Probability", String.valueOf(instanceConnectionSolverProbability), //
                "InstanceConnectionSolver_ProbabilityWithoutType", String.valueOf(instanceConnectionSolverProbabilityWithoutType) //
        );
    }

}
