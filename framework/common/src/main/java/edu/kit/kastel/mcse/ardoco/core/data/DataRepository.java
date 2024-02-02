/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.io.Serializable;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * This class represents a data repository that can be used to store and fetch certain data ({@link PipelineStepData}. Data can be added and fetched with the
 * help of a data identifier (as string). Fetching also needs the necessary class of data that is expected.
 */
public class DataRepository implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(DataRepository.class);

    private final SortedMap<String, PipelineStepData> data;

    public DataRepository() {
        this.data = new TreeMap<>();
        addData(GlobalConfiguration.ID, new GlobalConfiguration());
    }

    /**
     * Returns the {@link GlobalConfiguration} stored within the provided {@link DataRepository}.
     * 
     * @return the data
     */
    public final GlobalConfiguration getGlobalConfiguration() {
        return getData(GlobalConfiguration.ID, GlobalConfiguration.class).orElseThrow();
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
     * @param dataRepository data repository
     */
    public void addAllData(DataRepository dataRepository) {
        this.data.putAll(dataRepository.data);
    }

    /**
     * Creates a deep copy of the data repository using serialization.
     *
     * @return deep copy of the data repository
     */
    @DeepCopy
    public DataRepository deepCopy() {
        return DataRepositoryHelper.deepCopy(this);
    }
}
