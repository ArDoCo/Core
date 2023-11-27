/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.informants;

import java.util.Optional;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * The model extractor extracts the instances and relations via a connector. The extracted items are stored in a model
 * extraction state.
 */
public final class ArCoTLModelProviderInformant extends Informant {
    private static final String MODEL_STATES_DATA = "ModelStatesData";
    private final Extractor extractor;

    // Needed for Configuration Generation
    @SuppressWarnings("unused")
    private ArCoTLModelProviderInformant() {
        super(null, null);
        this.extractor = null;
    }

    /**
     * Instantiates a new model provider to extract information into the {@link DataRepository}.
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

        Model extractedModel = null;
        if (extractor instanceof CodeExtractor codeExtractor) {
            extractedModel = codeExtractor.readInCodeModel();
        }

        if (extractedModel == null) {
            logger.info("Extracting code model.");
            extractedModel = extractor.extractModel();
            if (extractor instanceof CodeExtractor codeExtractor && extractedModel instanceof CodeModel codeModel) {
                codeExtractor.writeOutCodeModel(codeModel);
            }
        }
        addModelStateToDataRepository(extractor.getModelId(), extractedModel);
    }

    private void addModelStateToDataRepository(String modelId, Model model) {
        var dataRepository = getDataRepository();
        Optional<ModelStates> modelStatesOptional = dataRepository.getData(MODEL_STATES_DATA, ModelStates.class);
        var modelStates = modelStatesOptional.orElseGet(ModelStates::new);

        modelStates.addModel(modelId, model);

        if (modelStatesOptional.isEmpty()) {
            dataRepository.addData(MODEL_STATES_DATA, modelStates);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
