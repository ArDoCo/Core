/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a data repository that can be used to store and fetch certain data ({@link PipelineStepData}. Data can be added and fetched with the
 * help of a data identifier (as string). Fetching also needs the necessary class of data that is expected.
 */
public class DataRepository implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(DataRepository.class);

    private Map<String, PipelineStepData> data;

    public DataRepository() {
        this.data = new HashMap<>();
    }

    /**
     * Returns data with the given identifier and casts the {@link PipelineStepData} into the given class, if possible. If data with such identifier does not
     * exist or cannot be cast, this method will return an empty Optional
     *
     * @param identifier Data identifier string
     * @param clazz      class that the data should have
     * @param <T>        Type of data that is expected and cast into
     * @return Optional containing the requested data cast into the given class. The optional is empty is data could not be found or casting was unsuccessful.
     */
    public <T extends PipelineStepData> Optional<T> getData(String identifier, Class<T> clazz) {
        var possibleData = data.get(identifier);
        if (possibleData != null) {
            return possibleData.asPipelineStepData(clazz);
        }
        logger.warn("Could not find data for id '{}'", identifier);
        return Optional.empty();
    }

    /**
     * Adds data to this repository using the identifier. If data with the given identifier already exists, overwrites it.
     *
     * @param identifier       Data identifier
     * @param pipelineStepData Data that should be saved
     */
    public void addData(String identifier, PipelineStepData pipelineStepData) {
        if (data.put(identifier, pipelineStepData) != null) {
            logger.warn("Overriding data with identifier '{}'", identifier);
        }
    }

    /**
     * Adds all data to the existing repository using the provided repository.
     *
     * @param dataRepository data repository used to override the existing repository.
     */
    public void addAllData(DataRepository dataRepository) {
        var copy = dataRepository.deepCopy();
        this.data.putAll(copy.data);
    }

    /**
     * Creates a deep copy of the data repository using serialization.
     *
     * @return deep copy of the data repository
     */
    public DataRepository deepCopy() {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(byteArrayOutputStream).writeObject(this);
            var byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return (DataRepository) new ObjectInputStream(byteArrayInputStream).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
