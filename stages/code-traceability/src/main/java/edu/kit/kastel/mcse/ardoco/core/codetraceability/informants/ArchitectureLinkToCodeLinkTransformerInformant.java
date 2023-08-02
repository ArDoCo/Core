/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.CodeTraceabilityStateImpl;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ArchitectureLinkToCodeLinkTransformerInformant extends Informant {

    public ArchitectureLinkToCodeLinkTransformerInformant(DataRepository dataRepository) {
        super(ArchitectureLinkToCodeLinkTransformerInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        MutableSet<SadCodeTraceLink> sadCodeTracelinks = Sets.mutable.empty();

        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(getDataRepository());
        ConnectionStates connectionStates = DataRepositoryHelper.getConnectionStates(getDataRepository());
        if (modelStatesData == null || connectionStates == null) {
            return;
        }

        CodeModel codeModel = findCodeModel(modelStatesData);

        for (var traceLink : connectionStates.getConnectionState(Metamodel.CODE).getTraceLinks()) {
            var modelElement = traceLink.getModelElementUid();
            var mentionedCodeModelElements = findMentionedCodeModelElementsById(modelElement, codeModel);
            for (var mid : mentionedCodeModelElements) {
                sadCodeTracelinks.add(new SadCodeTraceLink(new EndpointTuple(traceLink.getEndpointTuple().firstEndpoint(), mid)));
            }
        }

        CodeTraceabilityState codeTraceabilityState = new CodeTraceabilityStateImpl();
        getDataRepository().addData(CodeTraceabilityState.ID, codeTraceabilityState);
        codeTraceabilityState.addSadCodeTraceLinks(sadCodeTracelinks);
    }

    private List<CodeCompilationUnit> findMentionedCodeModelElementsById(String modelElementId, CodeModel codeModel) {
        boolean isPackage = modelElementId.endsWith("/");
        if (isPackage) {
            return findAllClassesInPackage(modelElementId, codeModel);
        }
        return findCompilationUnitById(modelElementId, codeModel);
    }

    private List<CodeCompilationUnit> findAllClassesInPackage(String modelElementId, CodeModel codeModel) {
        List<CodeCompilationUnit> codeCompilationUnits = new ArrayList<>();
        for (var codeCompilationUnit : codeModel.getEndpoints()) {
            var path = codeCompilationUnit.getPath();
            if (path.contains(modelElementId)) {
                codeCompilationUnits.add(codeCompilationUnit);
            }
        }
        if (codeCompilationUnits.isEmpty()) {
            throw new IllegalStateException("Could not find any code for " + modelElementId);
        }
        return codeCompilationUnits;
    }

    private List<CodeCompilationUnit> findCompilationUnitById(String modelElementId, CodeModel codeModel) {
        for (var codeCompilationUnit : codeModel.getEndpoints()) {
            if (codeCompilationUnit.getPath().equals(modelElementId)) {
                return List.of(codeCompilationUnit);
            }
        }
        throw new IllegalStateException("Could not find model element " + modelElementId);
    }

    private CodeModel findCodeModel(ModelStates models) {
        for (var modelId : models.modelIds()) {
            var model = models.getModel(modelId);
            if (model instanceof CodeModel codeModel) {
                return codeModel;
            }
        }
        return null;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }
}
