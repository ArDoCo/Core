/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.informants.ModelProviderInformant;

/**
 * Agent that provides information from models.
 */
public class ModelProviderAgent extends PipelineAgent {

    private final List<Informant> informants;

    /**
     * Instantiates a new model provider agent.
     * The constructor takes a list of ModelConnectors that are executed and used to extract information from models.
     * 
     * @param data            the DataRepository
     * @param modelConnectors the list of ModelConnectors that should be used
     */
    public ModelProviderAgent(DataRepository data, List<ModelConnector> modelConnectors) {
        super(ModelProviderAgent.class.getSimpleName(), data);
        informants = new ArrayList<>();
        for (var modelConnector : modelConnectors) {
            informants.add(new ModelProviderInformant(data, modelConnector));
        }
    }

    /**
     * Private constructor such that the ConfigurationHelper can operate (i.e., the ConfigurationHelperTest does not fail).
     * This should never be called deliberately!
     * 
     * @param data the DataRepository
     */
    private ModelProviderAgent(DataRepository data) {
        super(ModelProviderAgent.class.getSimpleName(), data);
        informants = new ArrayList<>();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return new ArrayList<>(informants);
    }
}
