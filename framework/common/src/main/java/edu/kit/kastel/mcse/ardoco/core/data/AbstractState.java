/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;

/**
 * This abstract class represents a state that can be saved to the {@link DataRepository} as {@link PipelineStepData}.
 */
public abstract class AbstractState extends AbstractConfigurable implements PipelineStepData {
    protected final DataRepository dataRepository;

    /**
     * Constructor for a new state
     *
     * @param dataRepository the {@link DataRepository} this state is associated with
     */
    protected AbstractState(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * {@return the {@link DataRepository} that is used for saving and fetching data}
     */
    public DataRepository getDataRepository() {
        return this.dataRepository;
    }

    /**
     * {@return the {@link MetaData } of the pipeline}
     */
    public MetaData getMetaData() {
        return DataRepositoryHelper.getMetaData(dataRepository);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
