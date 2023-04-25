package edu.kit.kastel.mcse.ardoco.core.codetraceability.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.ArCoTLInformant;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InitialCodeTraceabilityAgent extends PipelineAgent {
    private final MutableList<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    public InitialCodeTraceabilityAgent(DataRepository dataRepository) {
        super(InitialCodeTraceabilityAgent.class.getSimpleName(), dataRepository);

        informants = Lists.mutable.of(new ArCoTLInformant(dataRepository));
        enabledInformants = informants.collect(Informant::getId);
    }

    @Override
    protected void initializeState() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var samCodeTraceabilityStates = DataRepositoryHelper.getSamCodeTraceabilityStates(dataRepository);

        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var inconsistencyState = samCodeTraceabilityStates.getSamCodeTraceabilityState(metamodel);
            //TODO
        }
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }
}
