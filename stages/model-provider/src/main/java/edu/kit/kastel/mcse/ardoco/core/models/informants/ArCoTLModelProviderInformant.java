/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.informants;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.ModelExtractionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * The model extractor extracts the instances and relations via an connector. The extracted items are stored in a model
 * extraction state.
 */
public final class ArCoTLModelProviderInformant extends Informant {
    private static final String MODEL_STATES_DATA = "ModelStatesData";
    private Extractor extractor;
    private ModelExtractionStateImpl modelState = null;

    // Needed for Configuration Generation
    @SuppressWarnings("unused")
    private ArCoTLModelProviderInformant() {
        super(null, null);
        this.extractor = null;
    }

    /**
     * Instantiates a new model provider that uses the provided {@link ModelConnector} to extract information into the {@link DataRepository}.
     *
     * @param dataRepository the data repository
     * @param extractor      the model connector
     */
    public ArCoTLModelProviderInformant(DataRepository dataRepository, Extractor extractor) {
        super("Extractor " + extractor.getClass().getSimpleName(), dataRepository);
        this.extractor = extractor;
    }

    @Override
    public void run() {
        if (extractor == null) {
            return;
        }
        addModelStateToDataRepository(extractor.getModelId(), extractor.extractModel());
    }

    private void addModelStateToDataRepository(String modelId, Model model) {
        var dataRepository = getDataRepository();
        var modelStates = dataRepository.getData(MODEL_STATES_DATA, ModelStates.class).orElseGet(ModelStates::new);

        modelStates.addModel(modelId, model);

        dataRepository.addData(MODEL_STATES_DATA, modelStates);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
