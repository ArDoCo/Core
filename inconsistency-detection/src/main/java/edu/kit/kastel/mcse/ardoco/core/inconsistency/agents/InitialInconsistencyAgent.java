/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.eclipse.collections.api.factory.Lists;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class InitialInconsistencyAgent extends InconsistencyAgent {
    private double threshold = 0.75d;

    public InitialInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private InitialInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState) {
        super(GenericInconsistencyConfig.class, text, textState, modelState, recommendationState, connectionState, inconsistencyState);
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new InitialInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState);
    }

    @Override
    public void exec() {
        this.inconsistencyState.addRecommendedInstances(this.recommendationState.getRecommendedInstances().toList());

        filterRecommendedInstances();
    }

    /**
     * Filter RecommendedInstances based on various heuristics. First, filter unlikely ones (low probability).
     */
    private void filterRecommendedInstances() {
        var filteredRecommendedInstances = Lists.mutable.<IRecommendedInstance> empty();
        for (var recommendedInstance : this.inconsistencyState.getRecommendedInstances()) {
            if (recommendedInstance.getProbability() > threshold && checkOne(recommendedInstance) && checkTwo(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }

        this.inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
    }

    /**
     * Check for ???
     *
     * @param recommendedInstance the {@link IRecommendedInstance} to check
     * @return
     */
    private boolean checkOne(IRecommendedInstance recommendedInstance) {
        // TODO
        return true;
    }

    /**
     * Check for ???
     *
     * @param recommendedInstance the {@link IRecommendedInstance} to check
     * @return
     */
    private boolean checkTwo(IRecommendedInstance recommendedInstance) {
        // TODO
        return true;
    }

}
