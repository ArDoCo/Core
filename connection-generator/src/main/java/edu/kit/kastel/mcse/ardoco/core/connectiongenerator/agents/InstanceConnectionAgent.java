package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

/**
 * This connector finds names of model instance in recommended instances.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionAgent.class)
public class InstanceConnectionAgent extends ConnectionAgent {

    private double probability;
    private double probabilityWithoutType;

    /**
     * Create the agent.
     */
    public InstanceConnectionAgent() {
        super(GenericConnectionConfig.class);
    }

    private InstanceConnectionAgent(//
            IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState,
            GenericConnectionConfig config) {
        super(GenericConnectionConfig.class, text, textState, modelState, recommendationState, connectionState);
        probability = config.instanceConnectionSolverProbability;
        probabilityWithoutType = config.instanceConnectionSolverProbabilityWithoutType;
    }

    @Override
    public ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new InstanceConnectionAgent(text, textState, modelState, recommendationState, connectionState, (GenericConnectionConfig) config);
    }

    /**
     * Executes the connector.
     */
    @Override
    public void exec() {
        findNamesOfModelInstancesInSupposedMappings();
    }

    /**
     * Seaches in the recommended instances of the recommendation state for similar names to extracted instances. If
     * some are found the instance link is added to the connection state.
     */
    private void findNamesOfModelInstancesInSupposedMappings() {
        ImmutableList<IRecommendedInstance> ris = recommendationState.getRecommendedInstances();
        for (IModelInstance i : modelState.getInstances()) {
            ImmutableList<IRecommendedInstance> mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(i, ris);

            ImmutableList<IRecommendedInstance> mostLikelyRiWithoutType = mostLikelyRi.select(ri -> !ri.getTypeMappings().isEmpty());
            mostLikelyRiWithoutType.forEach(ml -> connectionState.addToLinks(ml, i, probabilityWithoutType));
            mostLikelyRi.forEach(ml -> connectionState.addToLinks(ml, i, probability));
        }
    }

}
