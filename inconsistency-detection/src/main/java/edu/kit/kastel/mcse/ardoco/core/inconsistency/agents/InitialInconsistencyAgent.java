/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractInformant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.OccasionFilter;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.RecommendedInstanceProbabilityFilter;

public class InitialInconsistencyAgent extends InconsistencyAgent {
    private final MutableList<AbstractInformant> filters;

    @Configurable
    private List<String> enabledFilters;

    public InitialInconsistencyAgent(DataRepository dataRepository) {
        super("InitialInconsistencyAgent", dataRepository);

        filters = Lists.mutable.of(new RecommendedInstanceProbabilityFilter(dataRepository), new OccasionFilter(dataRepository));
        enabledFilters = filters.collect(AbstractInformant::getId);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var inconsistencyState = inconsistencyStates.getInconsistencyState(metamodel);
            var recommendationState = recommendationStates.getRecommendationState(metamodel);

            inconsistencyState.addRecommendedInstances(recommendationState.getRecommendedInstances().toList());
        }

        for (var filter : findByClassName(enabledFilters, filters)) {
            this.addPipelineStep(filter);
        }

        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        filters.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }
}
