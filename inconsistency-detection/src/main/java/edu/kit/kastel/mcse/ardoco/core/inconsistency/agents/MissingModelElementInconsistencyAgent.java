package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.ArrayList;

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
import edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures.MissingModelInstanceInconsistency;

@MetaInfServices(InconsistencyAgent.class)
public class MissingModelElementInconsistencyAgent extends InconsistencyAgent {

    public MissingModelElementInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private MissingModelElementInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, GenericInconsistencyConfig inconsistencyConfig) {
        super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, GenericInconsistencyConfig.class, text, textState, modelState, recommendationState,
                connectionState, inconsistencyState);
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new MissingModelElementInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (GenericInconsistencyConfig) config);
    }

    @Override
    public void exec() {
        var recommendedInstances = new ArrayList<>(recommendationState.getRecommendedInstances());

        // remove all recommended instances that were used in an instanceLink (trace link)
        for (var tracelink : connectionState.getInstanceLinks()) {
            var textualInstance = tracelink.getTextualInstance();
            recommendedInstances.remove(textualInstance);
        }

        for (var recommendedInstance : recommendedInstances) {
            MissingModelInstanceInconsistency inconsistency = new MissingModelInstanceInconsistency(recommendedInstance);
            inconsistencyState.addInconsistency(inconsistency);
        }

    }
}
