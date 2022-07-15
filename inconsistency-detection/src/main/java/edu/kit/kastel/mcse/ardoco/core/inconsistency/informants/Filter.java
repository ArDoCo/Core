/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * A Filter is a special kind of Informant that takes the existing
 * {@link edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance}s and removes some of
 * them based on a given heuristic.
 *
 * Heuristics are implemented by creating an implementation of {@code filterRecommendedInstances}.
 */
public abstract class Filter extends Informant {

    protected Filter(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);

        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            var inconsistencyState = inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
            filterRecommendedInstances(inconsistencyState);
        }
    }

    protected abstract void filterRecommendedInstances(InconsistencyState inconsistencyState);
}
