/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;

/**
 * This class helps to access {@link DataRepository DataRepositories}. It provides methods to access the different
 * {@link edu.kit.kastel.informalin.data.PipelineStepData} that is stored within the repository that are used within ArDoCo.
 */
public final class DataRepositoryHelper {

    private DataRepositoryHelper() {
        super();
    }

    /**
     * Checks whether there is {@link ProjectPipelineData} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link ProjectPipelineData} within the {@link DataRepository}; else, false
     */
    public static boolean hasProjectPipelineData(DataRepository dataRepository) {
        return dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).isPresent();
    }

    /**
     * Returns the {@link ProjectPipelineData} stored within the provided {@link DataRepository}.
     * This does not check if there actually is one and will fail and throw an {@link java.util.NoSuchElementException} if the data is not present.
     * To make sure that there is data present, use {@link #hasProjectPipelineData(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the data
     */
    public static ProjectPipelineData getProjectPipelineData(DataRepository dataRepository) {
        return dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).orElseThrow();
    }

    /**
     * Checks whether there is annotated {@link Text} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link Text} within the {@link DataRepository}; else, false
     */
    public static boolean hasAnnotatedText(DataRepository dataRepository) {
        return dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).isPresent();
    }

    /**
     * Returns the {@link Text} stored within the provided {@link DataRepository}.
     * This does not check if there actually is one and will fail and throw an {@link java.util.NoSuchElementException} if the data is not present.
     * To make sure that there is data present, use {@link #hasAnnotatedText(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the text
     */
    public static Text getAnnotatedText(DataRepository dataRepository) {
        return dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
    }

    /**
     * Checks whether there is {@link TextState} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link TextState} within the {@link DataRepository}; else, false
     */
    public static boolean hasTextState(DataRepository dataRepository) {
        return dataRepository.getData(TextState.ID, TextState.class).isPresent();
    }

    /**
     * Returns the {@link TextState} stored within the provided {@link DataRepository}.
     * This does not check if there actually is one and will fail and throw an {@link java.util.NoSuchElementException} if the state is not present.
     * To make sure that there is data present, use {@link #hasTextState(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static TextState getTextState(DataRepository dataRepository) {
        return dataRepository.getData(TextState.ID, TextState.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link ModelStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link ModelStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasModelStatesData(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).isPresent();
    }

    /**
     * Returns the {@link ModelStates} stored within the provided {@link DataRepository}.
     * This does not check if there actually is one and will fail and throw an {@link java.util.NoSuchElementException} if the state is not present.
     * To make sure that there is data present, use {@link #hasModelStatesData(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static ModelStates getModelStatesData(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link RecommendationStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link RecommendationStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasRecommendationStates(DataRepository dataRepository) {
        return dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).isPresent();
    }

    /**
     * Returns the {@link RecommendationStates} stored within the provided {@link DataRepository}.
     * This does not check if there actually is one and will fail and throw an {@link java.util.NoSuchElementException} if the state is not present.
     * To make sure that there is data present, use {@link #hasRecommendationStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static RecommendationStates getRecommendationStates(DataRepository dataRepository) {
        return dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link ConnectionStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link ConnectionStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(ConnectionStates.ID, ConnectionStates.class).isPresent();
    }

    /**
     * Returns the {@link ConnectionStates} stored within the provided {@link DataRepository}.
     * This does not check if there actually is one and will fail and throw an {@link java.util.NoSuchElementException} if the state is not present.
     * To make sure that there is data present, use {@link #hasConnectionStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static ConnectionStates getConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link InconsistencyStates} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link InconsistencyStates} within the {@link DataRepository}; else, false
     */
    public static boolean hasInconsistencyStates(DataRepository dataRepository) {
        return dataRepository.getData(InconsistencyStates.ID, InconsistencyStates.class).isPresent();
    }

    /**
     * Returns the {@link InconsistencyStates} stored within the provided {@link DataRepository}.
     * This does not check if there actually is one and will fail and throw an {@link java.util.NoSuchElementException} if the state is not present.
     * To make sure that there is data present, use {@link #hasInconsistencyStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static InconsistencyStates getInconsistencyStates(DataRepository dataRepository) {
        return dataRepository.getData(InconsistencyStates.ID, InconsistencyStates.class).orElseThrow();
    }

    /**
     * Put the given {@link PreprocessingData} into the given {@link DataRepository}. This will override existing data!
     * 
     * @param dataRepository    the dataRepository
     * @param preprocessingData the preprocessingData
     */
    public static void putPreprocessingData(DataRepository dataRepository, PreprocessingData preprocessingData) {
        dataRepository.addData(PreprocessingData.ID, preprocessingData);
    }
}
