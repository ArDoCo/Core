/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.informants;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.ModelExtractionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * The model extractor extracts the instances and relations via an connector. The extracted items are stored in a model
 * extraction state.
 */
public final class ModelProviderInformant extends Informant {
    private static final String MODEL_STATES_DATA = "ModelStatesData";
    private ModelConnector modelConnector;
    private ModelExtractionStateImpl modelState = null;

    // Needed for Configuration Generation
    @SuppressWarnings("unused")
    private ModelProviderInformant() {
        super(null, null);
        this.modelConnector = null;
    }

    /**
     * Instantiates a new model provider that uses the provided {@link ModelConnector} to extract information into the {@link DataRepository}.
     *
     * @param dataRepository the data repository
     * @param modelConnector the model connector
     */
    public ModelProviderInformant(DataRepository dataRepository, ModelConnector modelConnector) {
        super("ModelProvider " + modelConnector.getModelId(), dataRepository);
        this.modelConnector = modelConnector;
    }

    @Override
    public void run() {
        if (modelConnector == null) {
            return;
        }
        ImmutableList<ModelInstance> instances = modelConnector.getInstances();
        modelState = new ModelExtractionStateImpl(modelConnector.getModelId(), modelConnector.getMetamodel(), instances);

        addModelStateToDataRepository();
    }

    private void addModelStateToDataRepository() {
        var dataRepository = getDataRepository();
        var optionalData = dataRepository.getData(MODEL_STATES_DATA, ModelStates.class);
        ModelStates modelStates;
        modelStates = optionalData.orElseGet(ModelStates::new);
        modelStates.addModelExtractionState(modelConnector.getModelId(), modelState);
        dataRepository.addData(MODEL_STATES_DATA, modelStates);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
