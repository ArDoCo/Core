/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
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
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCoForSadSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

class SadSamCodeTraceabilityLinkRecoveryEvaluationIT extends TraceabilityLinkRecoveryEvaluation {
    @Override
    protected ArDoCoRunner getAndSetupRunner(CodeProject codeProject) {
        String name = codeProject.name().toLowerCase();
        Project textProject = codeProject.getProject();
        File textInput = textProject.getTextFile();
        File inputArchitectureModel = codeProject.getProject().getModelFile();
        File inputCode = getInputCode(codeProject);
        Map<String, String> additionalConfigsMap = getAdditionalConfigsMap();

        var runner = new ArDoCoForSadSamCodeTraceabilityLinkRecovery(name);
        runner.setUp(textInput, inputArchitectureModel, ArchitectureModelType.PCM, inputCode, additionalConfigsMap, outputDir);
        return runner;
    }

    @Override
    protected ImmutableList<String> createTraceLinkStringList(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getTransitiveTraceLinks();

        MutableList<String> resultsMut = Lists.mutable.empty();
        for (var traceLink : traceLinks) {
            EndpointTuple endpointTuple = traceLink.getEndpointTuple();
            var codeElement = (CodeCompilationUnit) endpointTuple.secondEndpoint();
            String codeElementString = codeElement.toString() + "#" + codeElement.getName();

            String sentenceNumber = String.valueOf(((SadSamTraceLink) traceLink.getFirstTraceLink()).getSentenceNumber() + 1);

            String traceLinkString = TestUtil.createTraceLinkString(sentenceNumber, codeElementString);
            resultsMut.add(traceLinkString);
        }
        return resultsMut.toImmutable();
    }

    @Override
    protected ImmutableList<String> getGoldStandard(CodeProject codeProject) {
        MutableList<String> goldStandard = Lists.mutable.empty();

        ImmutableList<String> samCodeGoldStandard = codeProject.getSamCodeGoldStandard();
        ImmutableList<String> sadSamGoldStandard = codeProject.getProject().getTlrGoldStandard();

        var samCodeGoldStandardMultiMap = samCodeGoldStandard.collect(tl -> tl.split(",")).groupBy(tl -> tl[0]).collectValues(tl -> tl[1]);
        var sadSamGoldStandardMultiMap = sadSamGoldStandard.collect(tl -> tl.split(",")).groupBy(tl -> tl[0]).collectValues(tl -> tl[1]);

        for (var modelId : sadSamGoldStandardMultiMap.keysView()) {
            var sentenceNumbers = sadSamGoldStandardMultiMap.get(modelId);
            for (var codeId : samCodeGoldStandardMultiMap.get(modelId)) {
                for (var sentenceNumber : sentenceNumbers) {
                    String traceLink = TestUtil.createTraceLinkString(String.valueOf(sentenceNumber), codeId);
                    goldStandard.add(traceLink);
                }
            }
        }

        return goldStandard.sortThis().toImmutable();
    }

    @Override
    protected ExpectedResults getExpectedResults(CodeProject codeProject) {
        return codeProject.getExpectedResultsForSadSamCode();
    }

    @Override
    protected int getConfusionMatrixSum(ArDoCoResult arDoCoResult) {
        DataRepository dataRepository = arDoCoResult.dataRepository();

        Text text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        int sentences = text.getSentences().size();

        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        Model codeModel = modelStatesData.getModel(CodeModelType.CODE_MODEL.getModelId());
        var codeModelEndpoints = codeModel.getEndpoints().size();

        return sentences * codeModelEndpoints;
    }
}
