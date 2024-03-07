/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration;

import static edu.kit.kastel.mcse.ardoco.tlr.tests.integration.TraceLinkEvaluationIT.OUTPUT;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;

class SamCodeTraceabilityLinkRecoveryEvaluation extends TraceabilityLinkRecoveryEvaluation<CodeProject> {

    @Override
    protected boolean resultHasRequiredData(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSamCodeTraceLinks();
        return !traceLinks.isEmpty();
    }

    @Override
    protected ArDoCoForSamCodeTraceabilityLinkRecovery getAndSetupRunner(CodeProject codeProject) {
        String name = codeProject.name().toLowerCase();
        File inputCode = getInputCode(codeProject);
        File inputArchitectureModel = codeProject.getModelFile();
        SortedMap<String, String> additionalConfigsMap = new TreeMap<>();
        File outputDir = new File(OUTPUT);

        var runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(name);
        runner.setUp(inputArchitectureModel, ArchitectureModelType.PCM, inputCode, additionalConfigsMap, outputDir);
        return runner;
    }

    @Override
    protected ImmutableList<String> createTraceLinkStringList(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSamCodeTraceLinks();

        return TraceLinkUtilities.getSamCodeTraceLinksAsStringList(Lists.immutable.ofAll(traceLinks));
    }

    @Override
    protected ImmutableList<String> getGoldStandard(CodeProject codeProject) {
        ImmutableList<String> samCodeGoldStandard = codeProject.getSamCodeGoldStandard();
        return samCodeGoldStandard;
    }

    @Override
    protected ImmutableList<String> enrollGoldStandard(ImmutableList<String> goldStandard, ArDoCoResult result) {
        return enrollGoldStandardForCode(goldStandard, result);
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
