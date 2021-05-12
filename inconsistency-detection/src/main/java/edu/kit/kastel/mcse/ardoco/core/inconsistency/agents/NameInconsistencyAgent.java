package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class NameInconsistencyAgent extends InconsistencyAgent {

    public NameInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private NameInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, GenericInconsistencyConfig inconsistencyConfig) {
        super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, GenericInconsistencyConfig.class, text, textState, modelState, recommendationState,
                connectionState, inconsistencyState);
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new NameInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (GenericInconsistencyConfig) config);

    }

    @Override
    public void exec() {
        // TODO Auto-generated method stub
        System.out.println("Executing NameInconsistencyAgent");
    }

}
