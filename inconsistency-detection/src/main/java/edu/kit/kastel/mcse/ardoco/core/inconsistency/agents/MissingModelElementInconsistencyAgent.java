package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.eclipse.collections.api.factory.Lists;
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

    private double threshold = 0.7d;

    public MissingModelElementInconsistencyAgent() {
        super(InconsistencyConfig.class);
    }

    private MissingModelElementInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, InconsistencyConfig inconsistencyConfig) {
        super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, InconsistencyConfig.class, text, textState, modelState, recommendationState, connectionState,
                inconsistencyState);
        threshold = inconsistencyConfig.getMissingModelInstanceInconsistencyThreshold();
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new MissingModelElementInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (InconsistencyConfig) config);
    }

    @Override
    public void exec() {
        var recommendedInstances = Lists.mutable.ofAll(recommendationState.getRecommendedInstances());

        // remove all recommended instances that were used in an instanceLink (trace link)
        for (var tracelink : connectionState.getInstanceLinks()) {
            var textualInstance = tracelink.getTextualInstance();
            recommendedInstances.remove(textualInstance);
        }

        // TODO set and fine-tune a threshold to filter some recommended instances
        for (var recommendedInstance : recommendedInstances) {
            var confidence = recommendedInstance.getProbability();
            if (confidence >= threshold) {
                inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency(recommendedInstance));
            }

        }

    }
}
