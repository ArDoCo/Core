/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractFilter;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.filters.OccasionFilter;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.filters.RecommendedInstanceProbabilityFilter;

public class InitialInconsistencyAgent extends InconsistencyAgent {
    private final List<AbstractFilter> filters;

    @Configurable
    private List<String> enabledFilters;

    public InitialInconsistencyAgent(DataRepository dataRepository) {
        super("InitialInconsistencyAgent", dataRepository);

        filters = List.of(new RecommendedInstanceProbabilityFilter(dataRepository), new OccasionFilter(dataRepository));
        enabledFilters = filters.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = InconsistencyChecker.getModelStatesData(dataRepository);
        var recommendationStates = InconsistencyChecker.getRecommendationStates(dataRepository);
        var inconsistencyStates = InconsistencyChecker.getInconsistencyStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var inconsistencyState = inconsistencyStates.getInconsistencyState(metamodel);
            var recommendationState = recommendationStates.getRecommendationState(metamodel);

            inconsistencyState.addRecommendedInstances(recommendationState.getRecommendedInstances().toList());
        }

        for (var extractor : findByClassName(enabledFilters, filters)) {
            this.addPipelineStep(extractor);
        }

        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        filters.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }
}
