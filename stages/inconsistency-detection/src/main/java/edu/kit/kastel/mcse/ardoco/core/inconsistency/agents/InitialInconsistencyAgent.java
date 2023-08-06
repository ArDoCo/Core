/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.OccasionFilter;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.RecommendedInstanceProbabilityFilter;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.UnwantedWordsFilter;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InitialInconsistencyAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledFilters;

    public InitialInconsistencyAgent(DataRepository dataRepository) {
        super(InitialInconsistencyAgent.class.getSimpleName(), dataRepository,
                List.of(new RecommendedInstanceProbabilityFilter(dataRepository), new OccasionFilter(dataRepository), new UnwantedWordsFilter(dataRepository)));
        enabledFilters = getInformantIds();
    }

    @Override
    protected void initializeState() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);
        for (var model : modelStates.extractionModelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var inconsistencyState = inconsistencyStates.getInconsistencyState(metamodel);
            var recommendationState = recommendationStates.getRecommendationState(metamodel);

            inconsistencyState.addRecommendedInstances(recommendationState.getRecommendedInstances().toList());
        }
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledFilters, getInformants());
    }
}
