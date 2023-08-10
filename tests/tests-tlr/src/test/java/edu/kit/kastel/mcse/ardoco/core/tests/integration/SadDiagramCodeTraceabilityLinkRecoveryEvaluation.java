/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.tests.integration.TraceLinkEvaluationIT.OUTPUT;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoSadDiagramCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.tests.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class SadDiagramCodeTraceabilityLinkRecoveryEvaluation extends TraceabilityLinkRecoveryEvaluation {

    @Override
    protected boolean resultHasRequiredData(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSadCodeTraceLinks();
        return !traceLinks.isEmpty();
    }

    @Override
    protected ArDoCoRunner getAndSetupRunner(CodeProject codeProject) {
        String name = codeProject.name().toLowerCase();
        Project textProject = codeProject.getProject();
        File textInput = textProject.getTextFile();
        File inputArchitectureModel = codeProject.getProject().getModelFile();
        File inputCode = getInputCode(codeProject);
        Map<String, String> additionalConfigsMap = new HashMap<>();
        File outputDir = new File(OUTPUT);

        var runner = new ArDoCoSadDiagramCodeTraceabilityLinkRecovery(name);
        runner.setUp(textInput, DiagramProject.byCodeProject(codeProject).getDiagramDirectory(), inputCode, additionalConfigsMap, outputDir);
        return runner;
    }

    @Override
    protected void compareResults(EvaluationResults<String> results, ExpectedResults expectedResults) {
        logger.debug("Disable comparison, because transitive tracelinks are better");
    }

    @Override
    protected ImmutableList<String> createTraceLinkStringList(ArDoCoResult arDoCoResult) {
        var traceLinks = arDoCoResult.getSadCodeTraceLinks();
        return TraceLinkUtilities.getSadCodeTraceLinksAsStringList(Lists.immutable.ofAll(traceLinks));
    }

    @Override
    protected ImmutableList<String> getGoldStandard(CodeProject codeProject) {
        return codeProject.getSadCodeGoldStandard();
    }

    @Override
    protected ImmutableList<String> enrollGoldStandard(ImmutableList<String> goldStandard, ArDoCoResult result) {
        return enrollGoldStandardForCode(goldStandard, result);
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
