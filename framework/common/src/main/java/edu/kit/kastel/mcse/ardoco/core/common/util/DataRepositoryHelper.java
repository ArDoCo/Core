/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData;
import edu.kit.kastel.mcse.ardoco.core.api.InputTextData;
import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DeepCopy;
import edu.kit.kastel.mcse.ardoco.core.data.MetaData;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;

/**
 * This class helps to access {@link DataRepository DataRepositories}. It provides methods to access the different {@link PipelineStepData} that is stored
 * within the repository that are used within ArDoCo.
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
     * Returns the {@link ProjectPipelineData} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the data is not present. To make sure that there is data present, use
     * {@link #hasProjectPipelineData(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the data
     */
    public static ProjectPipelineData getProjectPipelineData(DataRepository dataRepository) {
        return dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).orElseThrow();
    }

    /**
     * Checks whether there is {@link MetaData} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link MetaData} within the {@link DataRepository}; else, false
     */
    public static boolean hasPipelineMetaData(DataRepository dataRepository) {
        return dataRepository.getData(MetaData.ID, MetaData.class).isPresent();
    }

    /**
     * Returns the {@link MetaData} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the data is not present. To make sure that there is data present, use
     * {@link #hasPipelineMetaData(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the data
     */
    public static MetaData getMetaData(DataRepository dataRepository) {
        return dataRepository.getData(MetaData.ID, MetaData.class).orElseThrow();
    }

    /**
     * Returns the input text as String stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and throw
     * an {@link java.util.NoSuchElementException} if the data is not present.
     *
     * @param dataRepository the DataRepository to access
     * @return the text
     */
    public static String getInputText(DataRepository dataRepository) {
        return dataRepository.getData(InputTextData.ID, InputTextData.class).orElseThrow().getText();
    }

    /**
     * Put the given input text into the given {@link DataRepository}. This will override existing data!
     *
     * @param dataRepository the dataRepository
     * @param inputText      the input inputText
     */
    public static void putInputText(DataRepository dataRepository, String inputText) {
        dataRepository.addData(InputTextData.ID, new InputTextData(inputText));
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
     * Returns the {@link Text} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and throw an
     * {@link java.util.NoSuchElementException} if the data is not present. To make sure that there is data present, use
     * {@link #hasAnnotatedText(DataRepository)}
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
     * Returns the {@link TextState} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and throw an
     * {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use {@link #hasTextState(DataRepository)}
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
     * Returns the {@link ModelStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and throw
     * an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasModelStatesData(DataRepository)}
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
     * Returns the {@link RecommendationStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail
     * and throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasRecommendationStates(DataRepository)}
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
     * Returns the {@link ConnectionStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasConnectionStates(DataRepository)}
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
     * Returns the {@link InconsistencyStates} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail and
     * throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasInconsistencyStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static InconsistencyStates getInconsistencyStates(DataRepository dataRepository) {
        if (hasInconsistencyStates(dataRepository)) {
            return dataRepository.getData(InconsistencyStates.ID, InconsistencyStates.class).orElseThrow();
        }
        return null;
    }

    /**
     * Checks whether there is {@link CodeTraceabilityState} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link CodeTraceabilityState} within the {@link DataRepository}; else, false
     */
    public static boolean hasCodeTraceabilityState(DataRepository dataRepository) {
        return dataRepository.getData(CodeTraceabilityState.ID, CodeTraceabilityState.class).isPresent();
    }

    /**
     * Returns the {@link CodeTraceabilityState} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail
     * and throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasInconsistencyStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static CodeTraceabilityState getCodeTraceabilityState(DataRepository dataRepository) {
        if (hasCodeTraceabilityState(dataRepository)) {
            return dataRepository.getData(CodeTraceabilityState.ID, CodeTraceabilityState.class).orElseThrow();
        }
        return null;
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

    /**
     * Checks whether there is {@link DiagramRecognitionState} stored within the provided {@link DataRepository}
     *
     * @param dataRepository the DataRepository to access
     * @return true, if there is {@link DiagramRecognitionState} within the {@link DataRepository}; else, false
     */
    public static boolean hasDiagramRecognitionState(DataRepository dataRepository) {
        return dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).isPresent();
    }

    /**
     * Returns the {@link DiagramRecognitionState} stored within the provided {@link DataRepository}. This does not check if there actually is one and will fail
     * and throw an {@link java.util.NoSuchElementException} if the state is not present. To make sure that there is data present, use
     * {@link #hasConnectionStates(DataRepository)}
     *
     * @param dataRepository the DataRepository to access
     * @return the state
     */
    public static DiagramRecognitionState getDiagramRecognitionState(DataRepository dataRepository) {
        return dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
    }

    /**
     * Save diagram directory as {@link InputDiagramData}.
     *
     * @param dataRepository   the data repository
     * @param diagramDirectory the diagram directory
     */
    public static void putDiagramDirectory(DataRepository dataRepository, File diagramDirectory) {
        InputDiagramData data = new InputDiagramData(diagramDirectory);
        dataRepository.addData(InputDiagramData.ID, data);

    }

    /**
     * {@return a deep copy of a serializable object using serialization}
     *
     * @param object the object to copy
     */
    @DeepCopy
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopy(T object) {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(byteArrayOutputStream).writeObject(object);
            var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return (T) new ObjectInputStream(byteArrayInputStream).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
