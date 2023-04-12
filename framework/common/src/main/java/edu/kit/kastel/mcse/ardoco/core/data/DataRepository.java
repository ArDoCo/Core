/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a data repository that can be used to store and fetch certain data ({@link PipelineStepData}.
 * Data can be added and fetched with the help of a data identifier (as string). Fetching also needs the necessary class
 * of data that is expected.
 */
public class DataRepository {
    private static final Logger logger = LoggerFactory.getLogger(DataRepository.class);

    private final Map<String, PipelineStepData> data;

    public DataRepository() {
        this.data = new HashMap<>();
    }

    /**
     * Returns data with the given identifier and casts the {@link PipelineStepData} into the given class, if possible.
     * If data with such identifier does not exist or cannot be cast, this method will return an empty Optional
     * 
     * @param identifier Data identifier string
     * @param clazz      class that the data should have
     * @return Optional containing the requested data cast into the given class. The optional is empty is data could not
     *         be found or casting was unsuccessful.
     * @param <T> Type of data that is expected and cast into
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
     * Adds data to this repository using the identifier. If data with the given identifier already exists, overwrites
     * it.
     * 
     * @param identifier       Data identifier
     * @param pipelineStepData Data that should be saved
     */
    public void addData(String identifier, PipelineStepData pipelineStepData) {
        if (data.put(identifier, pipelineStepData) != null) {
            logger.warn("Overriding data with identifier '{}'", identifier);
        }
    }

}
