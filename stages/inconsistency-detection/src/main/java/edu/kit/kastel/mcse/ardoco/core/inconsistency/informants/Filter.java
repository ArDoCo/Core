/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * A Filter is a special kind of Informant that takes the existing
 * {@link RecommendedInstance}s and removes some of
 * them based on a given heuristic.
 *
 * Heuristics are implemented by creating an implementation of {@code filterRecommendedInstances}.
 */
public abstract class Filter extends Informant {

    protected Filter(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    @Override
    public void process() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);

        for (var model : modelStates.extractionModelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            var inconsistencyState = inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
            filterRecommendedInstances(inconsistencyState);
        }
    }

    protected abstract void filterRecommendedInstances(InconsistencyState inconsistencyState);
}
