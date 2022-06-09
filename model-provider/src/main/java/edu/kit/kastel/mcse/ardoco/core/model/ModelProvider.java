/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStatesData;

/**
 * The model extractor extracts the instances and relations via an connector. The extracted items are stored in a model
 * extraction state.
 *
 * @author Sophie
 */
public final class ModelProvider extends AbstractPipelineStep {
    private static final String MODEL_STATES_DATA = "ModelStatesData";
    private final IModelConnector modelConnector;
    private Map<String, String> additionalSettings;
    private ModelExtractionState modelState = null;

    /**
     * Instantiates a new model provider.
     *
     * @param modelConnector the model connector
     */
    public ModelProvider(DataRepository dataRepository, IModelConnector modelConnector) {
        super("ModelProvider " + modelConnector.getModelId(), dataRepository);
        this.modelConnector = modelConnector;
    }

    public void setAdditionalSettings(Map<String, String> additionalSettings) {
        this.additionalSettings = additionalSettings;
    }

    /**
     * Returns the {@link ModelExtractionState}. Returns null if this step did not run previously.
     * 
     * @return the {@link ModelExtractionState} if the Provider did run. Else, null
     */
    public ModelExtractionState getModelState() {
        return modelState;
    }

    @Override
    public void run() {
        ImmutableList<IModelInstance> instances = modelConnector.getInstances();
        modelState = new ModelExtractionState(modelConnector.getModelId(), modelConnector.getMetamodel(), instances, additionalSettings);

        addModelStateToDataRepository();
    }

    private void addModelStateToDataRepository() {
        var dataRepository = getDataRepository();
        var optionalData = dataRepository.getData(MODEL_STATES_DATA, ModelStatesData.class);
        ModelStatesData modelStatesData;
        if (optionalData.isEmpty()) {
            modelStatesData = new ModelStatesData();
        } else {
            modelStatesData = optionalData.get();
        }
        modelStatesData.addModelState(modelConnector.getModelId(), modelState);
        dataRepository.addData(MODEL_STATES_DATA, modelStatesData);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
