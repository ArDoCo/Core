package edu.kit.kastel.mcse.ardoco.core.common.util;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;

public final class DataRepositoryHelper {

    private DataRepositoryHelper() {
        super();
    }

    public static IText getAnnotatedText(DataRepository dataRepository) {
        return dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
    }

    public static ITextState getTextState(DataRepository dataRepository) {
        return dataRepository.getData(ITextState.ID, ITextState.class).orElseThrow();
    }

    public static ModelStates getModelStatesData(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();
    }

    public static IRecommendationStates getRecommendationStates(DataRepository dataRepository) {
        return dataRepository.getData(IRecommendationStates.ID, IRecommendationStates.class).orElseThrow();
    }

    public static IConnectionStates getConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(IConnectionStates.ID, IConnectionStates.class).orElseThrow();
    }

    public static IInconsistencyStates getInconsistencyStates(DataRepository dataRepository) {
        return dataRepository.getData(IInconsistencyStates.ID, IInconsistencyStates.class).orElseThrow();
    }
}
