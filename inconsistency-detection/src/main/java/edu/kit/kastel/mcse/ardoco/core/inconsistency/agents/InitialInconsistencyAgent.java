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
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class InitialInconsistencyAgent extends InconsistencyAgent {
    private static final double THRESHOLD_NAME_PROBABILITY = 0.5;
    private static final double THRESHOLD_TYPE_PROBABILITY = 0.9;
    private static final double THRESHOLD_RI_PROBABILITY = 0.75d;

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
            if (performHeuristicsAndChecks(recommendedInstance)) {
                filteredRecommendedInstances.add(recommendedInstance);
            }
        }

        System.out.println(this.inconsistencyState.getRecommendedInstances().size());
        this.inconsistencyState.setRecommendedInstances(filteredRecommendedInstances);
        System.out.println(this.inconsistencyState.getRecommendedInstances().size());
    }

    private boolean performHeuristicsAndChecks(IRecommendedInstance recommendedInstance) {
        var checksArePositive = checkProbabilityOfBeingRecommendedInstance(recommendedInstance);
        checksArePositive = checksArePositive && checkProbabilitiesForNounMappingTypes(recommendedInstance);
        return checksArePositive && checkTwo(recommendedInstance);
    }

    private boolean checkProbabilityOfBeingRecommendedInstance(IRecommendedInstance recommendedInstance) {
        return recommendedInstance.getProbability() > THRESHOLD_RI_PROBABILITY;
    }

    /**
     * Check for probabilities of the types for the {@link INounMapping}s that are contained by the
     * {@link IRecommendedInstance}. If they exceed a threshold, then the check is positive
     *
     * @param recommendedInstance the {@link IRecommendedInstance} to check
     * @return true if the probabilities of the types exceed a threshold
     */
    private boolean checkProbabilitiesForNounMappingTypes(IRecommendedInstance recommendedInstance) {
        for (var type : recommendedInstance.getTypeMappings()) {
            if (type.getProbabilityForType() > THRESHOLD_TYPE_PROBABILITY) {
                return true;
            }
        }

        for (var name : recommendedInstance.getNameMappings()) {
            if (name.getProbabilityForName() > THRESHOLD_NAME_PROBABILITY) {
                return true;
            }
        }
        return false;
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
