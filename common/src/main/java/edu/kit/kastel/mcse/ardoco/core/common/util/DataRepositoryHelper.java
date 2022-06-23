/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;

public final class DataRepositoryHelper {

    private DataRepositoryHelper() {
        super();
    }

    public static Text getAnnotatedText(DataRepository dataRepository) {
        return dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
    }

    public static TextState getTextState(DataRepository dataRepository) {
        return dataRepository.getData(TextState.ID, TextState.class).orElseThrow();
    }

    public static ModelStates getModelStatesData(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();
    }

    public static RecommendationStates getRecommendationStates(DataRepository dataRepository) {
        return dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).orElseThrow();
    }

    public static ConnectionStates getConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
    }

    public static InconsistencyStates getInconsistencyStates(DataRepository dataRepository) {
        return dataRepository.getData(InconsistencyStates.ID, InconsistencyStates.class).orElseThrow();
    }
}
