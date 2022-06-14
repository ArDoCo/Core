/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;

public final class DataStructure {

    private final DataRepository dataRepository;

    public DataStructure(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public DataStructure createCopy() {
        // TODO better copy
        return new DataStructure(dataRepository);
    }

    public IConnectionState getConnectionState(String model) {
        var connectionStates = dataRepository.getData(IConnectionStates.ID, IConnectionStates.class).orElseThrow();
        var modelState = getModelState(model);
        return connectionStates.getConnectionState(modelState.getMetamodel());
    }

    public IInconsistencyState getInconsistencyState(String model) {
        var inconsistencyStates = dataRepository.getData(IInconsistencyStates.ID, IInconsistencyStates.class).orElseThrow();
        var modelState = getModelState(model);
        return inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
    }

    private ModelStates getModelStates() {
        var modelStates = dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();
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
        var recommendationStates = dataRepository.getData(IRecommendationStates.ID, IRecommendationStates.class).orElseThrow();
        return recommendationStates.getRecommendationState(mm);
    }

    public IText getText() {
        var preprocessingData = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow();
        return preprocessingData.getText();
    }

    private void ensureModel(String model) {
        if (!getModelIds().contains(model))
            throw new IllegalArgumentException("Model with Key " + model + " was not found");
    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }
}
