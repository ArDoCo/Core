/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

public record DataStructure(DataRepository dataRepository) {

    public ConnectionState getConnectionState(String model) {
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
        var modelState = getModelState(model);
        return connectionStates.getConnectionState(modelState.getMetamodel());
    }

    public InconsistencyState getInconsistencyState(String model) {
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);
        var modelState = getModelState(model);
        return inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
    }

    private ModelStates getModelStates() {
        return DataRepositoryHelper.getModelStatesData(dataRepository);
    }

    public List<String> getModelIds() {
        ModelStates modelStates = getModelStates();
        return Lists.mutable.ofAll(modelStates.modelIds());
    }

    public ModelExtractionState getModelState(String model) {
        ModelStates modelStates = getModelStates();
        return modelStates.getModelState(model);
    }

    public RecommendationState getRecommendationState(Metamodel mm) {
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        return recommendationStates.getRecommendationState(mm);
    }

    public Text getText() {
        var preprocessingData = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow();
        return preprocessingData.getText();
    }
}
