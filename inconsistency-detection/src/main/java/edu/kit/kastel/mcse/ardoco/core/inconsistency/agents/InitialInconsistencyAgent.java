/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.OccasionFilter;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.RecommendedInstanceProbabilityFilter;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.UnwantedWordsFilter;

public class InitialInconsistencyAgent extends PipelineAgent {
    private final MutableList<Informant> filters;

    @Configurable
    private List<String> enabledFilters;

    public InitialInconsistencyAgent(DataRepository dataRepository) {
        super(InitialInconsistencyAgent.class.getSimpleName(), dataRepository);

        filters = Lists.mutable.of(new RecommendedInstanceProbabilityFilter(dataRepository), new OccasionFilter(dataRepository), new UnwantedWordsFilter(
                dataRepository));
        enabledFilters = filters.collect(Informant::getId);
    }

    @Override
    protected void initializeState() {
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
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledFilters, filters);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        filters.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }
}
