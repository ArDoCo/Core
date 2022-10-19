/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.HoldElementsBackModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline.InconsistencyBaseline;

public class HoldBackRunResultsProducer {
    private File inputText;
    private File inputModel;
    private PcmXMLModelConnector pcmModel;

    public HoldBackRunResultsProducer() {
        super();
    }

    /**
     * Runs ArDoCo or the ArDoCo-backed baseline approach multiple times to produce results. The first run calls ArDoCo
     * normally, in further runs one element is held back each time (so that each element was held back once). This way,
     * we can simulate missing elements.
     * 
     * @param project             the project that should be run
     * @param useBaselineApproach set to true of the baseline approach should be used instead of ArDoCo
     * @return a map containing the mapping from ModelElement that was held back to the DataStructure that was produced
     *         when running ArDoCo without the ModelElement
     */
    public Map<ModelInstance, ArDoCoResult> produceHoldBackRunResults(Project project, boolean useBaselineApproach) {
        Map<ModelInstance, ArDoCoResult> runs = new HashMap<ModelInstance, ArDoCoResult>();

        inputModel = project.getModelFile();
        inputText = project.getTextFile();

        var holdElementsBackModelConnector = constructHoldElementsBackModelConnector();

        ArDoCo arDoCoBaseRun;
        try {
            arDoCoBaseRun = definePipelineBase(project, inputText, holdElementsBackModelConnector, useBaselineApproach);
        } catch (IOException e) {
            Assertions.fail(e);
            return runs;
        }
        arDoCoBaseRun.run();
        var baseRunData = new ArDoCoResult(arDoCoBaseRun.getDataRepository());
        runs.put(null, baseRunData);

        for (int i = 0; i < holdElementsBackModelConnector.numberOfActualInstances(); i++) {
            holdElementsBackModelConnector.setCurrentHoldBackIndex(i);
            var currentHoldBack = holdElementsBackModelConnector.getCurrentHoldBack();
            var currentRun = defineArDoCoWithPreComputedData(baseRunData, holdElementsBackModelConnector, useBaselineApproach);
            currentRun.run();
            runs.put(currentHoldBack, new ArDoCoResult(currentRun.getDataRepository()));
        }
        return runs;
    }

    private HoldElementsBackModelConnector constructHoldElementsBackModelConnector() {
        try {
            pcmModel = new PcmXMLModelConnector(inputModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HoldElementsBackModelConnector(pcmModel);
    }

    private static ArDoCo definePipelineBase(Project project, File inputText, HoldElementsBackModelConnector holdElementsBackModelConnector,
            boolean useInconsistencyBaseline) throws FileNotFoundException {
        ArDoCo arDoCo = new ArDoCo(project.name().toLowerCase());
        var dataRepository = arDoCo.getDataRepository();
        var additionalConfigs = project.getAdditionalConfigurations();

        arDoCo.addPipelineStep(ArDoCo.getTextProvider(inputText, additionalConfigs, dataRepository));

        addMiddleSteps(holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(ArDoCo.getInconsistencyChecker(additionalConfigs, dataRepository));
        }

        return arDoCo;
    }

    private static ArDoCo defineArDoCoWithPreComputedData(ArDoCoResult precomputedResults, HoldElementsBackModelConnector holdElementsBackModelConnector,
            boolean useInconsistencyBaseline) {
        var projectName = precomputedResults.getProjectName();
        ArDoCo arDoCo = new ArDoCo(projectName);
        var dataRepository = arDoCo.getDataRepository();

        var additionalConfigs = ArDoCo.loadAdditionalConfigs(null);
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
            arDoCo.addPipelineStep(ArDoCo.getInconsistencyChecker(additionalConfigs, dataRepository));
        }
        return arDoCo;
    }

    private static void addMiddleSteps(HoldElementsBackModelConnector holdElementsBackModelConnector, ArDoCo arDoCo, DataRepository dataRepository,
            Map<String, String> additionalConfigs) {
        arDoCo.addPipelineStep(new ModelProvider(dataRepository, holdElementsBackModelConnector));
        arDoCo.addPipelineStep(ArDoCo.getTextExtraction(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ArDoCo.getRecommendationGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ArDoCo.getConnectionGenerator(additionalConfigs, dataRepository));
    }
}
