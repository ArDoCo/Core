/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

public record DataStructure(DataRepository dataRepository) {

    public IConnectionState getConnectionState(String model) {
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
        var modelState = getModelState(model);
        return connectionStates.getConnectionState(modelState.getMetamodel());
    }

    public IInconsistencyState getInconsistencyState(String model) {
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);
        var modelState = getModelState(model);
        return inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
    }

    private ModelStates getModelStates() {
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        return modelStates;
    }

    public List<String> getModelIds() {
        ModelStates modelStates = getModelStates();
        return Lists.mutable.ofAll(modelStates.modelIds());
    }

    public IModelState getModelState(String model) {
        ModelStates modelStates = getModelStates();
        return modelStates.getModelState(model);
    }

    public IRecommendationState getRecommendationState(Metamodel mm) {
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        return recommendationStates.getRecommendationState(mm);
    }

    public IText getText() {
        var preprocessingData = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow();
        return preprocessingData.getText();
    }
}
