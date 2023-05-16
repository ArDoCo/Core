/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.tests.integration.TraceLinkEvaluationIT.OUTPUT;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

class SamCodeTraceabilityLinkRecoveryEvaluation extends TraceabilityLinkRecoveryEvaluation {

    @Override
    protected boolean resultHasRequiredData(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSamCodeTraceLinks();
        return !traceLinks.isEmpty();
    }

    @Override
    protected ArDoCoForSamCodeTraceabilityLinkRecovery getAndSetupRunner(CodeProject codeProject) {
        String name = codeProject.name().toLowerCase();
        File inputCode = getInputCode(codeProject);
        File inputArchitectureModel = codeProject.getProject().getModelFile();
        Map<String, String> additionalConfigsMap = new HashMap<>();
        File outputDir = new File(OUTPUT);

        var runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(name);
        runner.setUp(inputArchitectureModel, ArchitectureModelType.PCM, inputCode, additionalConfigsMap, outputDir);
        return runner;
    }

    @Override
    protected ImmutableList<String> createTraceLinkStringList(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSamCodeTraceLinks();

        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : traceLinks) {
            EndpointTuple endpointTuple = traceLink.getEndpointTuple();
            var modelElement = endpointTuple.firstEndpoint();
            var codeElement = (CodeCompilationUnit) endpointTuple.secondEndpoint();
            String codeElementString = codeElement.toString() + "#" + codeElement.getName();
            String traceLinkString = TestUtil.createTraceLinkString(modelElement.getId(), codeElementString);
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }

    @Override
    protected ImmutableList<String> getGoldStandard(CodeProject codeProject) {
        return codeProject.getSamCodeGoldStandard();
    }

    @Override
    protected ExpectedResults getExpectedResults(CodeProject codeProject) {
        return codeProject.getExpectedResultsForSamCode();
    }

    @Override
    protected int getConfusionMatrixSum(ArDoCoResult arDoCoResult) {
        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(arDoCoResult.dataRepository());
        Model codeModel = modelStatesData.getModel(CodeModelType.CODE_MODEL.getModelId());
        Model architectureModel = modelStatesData.getModel(ArchitectureModelType.PCM.getModelId());
        var codeModelEndpoints = codeModel.getEndpoints().size();
        var architectureModelEndpoints = architectureModel.getEndpoints().size();
        return codeModelEndpoints * architectureModelEndpoints;
    }

}