package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.TraceLinkGenerator;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.computationtree.Node;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ArCoTLInformant extends Informant {
    private static final Logger logger = LoggerFactory.getLogger(ArCoTLInformant.class);

    public ArCoTLInformant(DataRepository dataRepository) {
        super(ArCoTLInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var samCodeTraceabilityStates = DataRepositoryHelper.getSamCodeTraceabilityStates(dataRepository);

        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var samCodeTraceabilityState = samCodeTraceabilityStates.getSamCodeTraceabilityState(metamodel);

            //TODO
            ArchitectureModel architectureModel = null;
            CodeModel codeModel = null;
            Node root = TraceLinkGenerator.getRoot();
            var traceLinks = TraceLinkGenerator.generateTraceLinks(root, architectureModel, codeModel); //TODO
            samCodeTraceabilityState.addTraceLinks(traceLinks);
        }

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }

}
