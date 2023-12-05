/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public class HoldBackRunResultsProducer {
    private File inputText;
    private File inputModel;

    public HoldBackRunResultsProducer() {
        super();
    }

    /**
     * Runs ArDoCo or the ArDoCo-backed baseline approach multiple times to produce results. The first run calls ArDoCo
     * normally, in further runs one element is held back each time (so that each element was held back once). This way,
     * we can simulate missing elements.
     *
     * @param project             the project that should be run
     * @param useBaselineApproach set to true if the baseline approach should be used instead of ArDoCo
     * @return a map containing the mapping from ModelElement that was held back to the DataStructure that was produced
     *         when running ArDoCo without the ModelElement
     */
    public Map<ModelElement, ArDoCoResult> produceHoldBackRunResults(Project project, boolean useBaselineApproach) {
        Map<ModelElement, ArDoCoResult> runs = new LinkedHashMap<>();

        inputModel = project.getModelFile();
        inputText = project.getTextFile();

        HoldBackArCoTLModelProvider holdBackArCoTLModelProvider = new HoldBackArCoTLModelProvider(inputModel);

        ArDoCo arDoCoBaseRun;
        try {
            arDoCoBaseRun = definePipelineBase(project, inputText, holdBackArCoTLModelProvider, useBaselineApproach);
        } catch (IOException e) {
            Assertions.fail(e);
            return runs;
        }
        arDoCoBaseRun.run();
        var baseRunData = new ArDoCoResult(arDoCoBaseRun.getDataRepository());
        runs.put(null, baseRunData);

        for (int i = 0; i < holdBackArCoTLModelProvider.numberOfActualInstances(); i++) {
            holdBackArCoTLModelProvider.setCurrentHoldBackIndex(i);
            var currentHoldBack = holdBackArCoTLModelProvider.getCurrentHoldBack();
            var currentRun = defineArDoCoWithPreComputedData(baseRunData, holdBackArCoTLModelProvider, useBaselineApproach);
            currentRun.run();
            runs.put(currentHoldBack, new ArDoCoResult(currentRun.getDataRepository()));
        }
        return runs;
    }

    private static ArDoCo definePipelineBase(Project project, File inputText, HoldBackArCoTLModelProvider holdElementsBackModelConnector,
            boolean useInconsistencyBaseline) throws FileNotFoundException {
        ArDoCo arDoCo = new ArDoCo(project.name().toLowerCase());
        var dataRepository = arDoCo.getDataRepository();
        String text = CommonUtilities.readInputText(inputText);
        DataRepositoryHelper.putInputText(dataRepository, text);
        var additionalConfigs = project.getAdditionalConfigurations();

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));

        addMiddleSteps(holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
        }

        return arDoCo;
    }

    private static ArDoCo defineArDoCoWithPreComputedData(ArDoCoResult precomputedResults, HoldBackArCoTLModelProvider holdElementsBackModelConnector,
            boolean useInconsistencyBaseline) {
        var projectName = precomputedResults.getProjectName();
        ArDoCo arDoCo = new ArDoCo(projectName);
        var dataRepository = arDoCo.getDataRepository();

        var additionalConfigs = ConfigurationHelper.loadAdditionalConfigs(null);
        var optionalProject = Project.getFromName(projectName);
        if (optionalProject.isPresent()) {
            additionalConfigs = optionalProject.get().getAdditionalConfigurations();
        }

        var preprocessingData = new PreprocessingData(precomputedResults.getText());
        dataRepository.addData(PreprocessingData.ID, preprocessingData);

        addMiddleSteps(holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
        }
        return arDoCo;
    }

    private static void addMiddleSteps(HoldBackArCoTLModelProvider holdElementsBackModelConnector, ArDoCo arDoCo, DataRepository dataRepository,
            SortedMap<String, String> additionalConfigs) {
        arDoCo.addPipelineStep(holdElementsBackModelConnector.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
    }
}
