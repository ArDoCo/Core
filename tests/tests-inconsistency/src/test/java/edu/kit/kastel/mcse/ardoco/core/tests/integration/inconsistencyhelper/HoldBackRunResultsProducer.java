/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.PcmXmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public class HoldBackRunResultsProducer {
    private File inputText;
    private File inputModel;
    private PcmXmlModelConnector pcmModel;

    public HoldBackRunResultsProducer() {
        super();
    }

    /**
     * Runs ArDoCo or the ArDoCo-backed baseline approach multiple times to produce results. The first run calls ArDoCo normally, in further runs one element is
     * held back each time (so that each element was held back once). This way, we can simulate missing elements.
     *
     * @param goldStandardProject the project that should be run
     * @param useBaselineApproach set to true if the baseline approach should be used instead of ArDoCo
     * @return a map containing the mapping from ModelElement that was held back to the DataStructure that was produced when running ArDoCo without the
     * ModelElement
     */
    public Map<ModelInstance, ArDoCoResult> produceHoldBackRunResults(GoldStandardProject goldStandardProject, boolean useBaselineApproach) {
        Map<ModelInstance, ArDoCoResult> runs = new HashMap<ModelInstance, ArDoCoResult>();
        inputModel = goldStandardProject.getModelFile();
        inputText = goldStandardProject.getTextFile();

        var holdElementsBackModelConnector = constructHoldElementsBackModelConnector();

        ArDoCo arDoCoBaseRun;
        try {
            arDoCoBaseRun = definePipelineBase(goldStandardProject, inputText, holdElementsBackModelConnector, useBaselineApproach);
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
            var currentRun = defineArDoCoWithPreComputedData(baseRunData, goldStandardProject, holdElementsBackModelConnector, useBaselineApproach);
            currentRun.run();
            runs.put(currentHoldBack, new ArDoCoResult(currentRun.getDataRepository()));
        }
        return runs;
    }

    private HoldElementsBackModelConnector constructHoldElementsBackModelConnector() {
        try {
            pcmModel = new PcmXmlModelConnector(inputModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HoldElementsBackModelConnector(pcmModel);
    }

    private ArDoCo definePipelineBase(GoldStandardProject goldStandardProject, File inputText, HoldElementsBackModelConnector holdElementsBackModelConnector,
            boolean useInconsistencyBaseline) throws FileNotFoundException {
        ArDoCo arDoCo = new ArDoCo(goldStandardProject.getProjectName().toLowerCase());
        var dataRepository = arDoCo.getDataRepository();
        var additionalConfigs = goldStandardProject.getAdditionalConfigurations();

        addPreSteps(inputText, goldStandardProject, arDoCo, dataRepository, additionalConfigs);
        addMiddleSteps(goldStandardProject, holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
        }

        return arDoCo;
    }

    private ArDoCo defineArDoCoWithPreComputedData(ArDoCoResult precomputedResults, GoldStandardProject goldStandardProject,
            HoldElementsBackModelConnector holdElementsBackModelConnector, boolean useInconsistencyBaseline) {
        var projectName = precomputedResults.getProjectName();
        ArDoCo arDoCo = new ArDoCo(projectName);
        var dataRepository = arDoCo.getDataRepository();
        var additionalConfigs = goldStandardProject.getAdditionalConfigurations();

        addPreSteps(precomputedResults, dataRepository);
        addMiddleSteps(goldStandardProject, holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
        }
        return arDoCo;
    }

    protected void addMiddleSteps(GoldStandardProject goldStandardProject, HoldElementsBackModelConnector holdElementsBackModelConnector, ArDoCo arDoCo,
            DataRepository dataRepository, SortedMap<String, String> additionalConfigs) {
        arDoCo.addPipelineStep(new ModelProviderInformant(dataRepository, holdElementsBackModelConnector));
        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
    }

    protected void addPreSteps(File inputText, GoldStandardProject goldStandardProject, ArDoCo arDoCo, DataRepository dataRepository,
            Map<String, String> additionalConfigs) {
        String text = CommonUtilities.readInputText(inputText);
        DataRepositoryHelper.putInputText(dataRepository, text);

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));
    }

    protected void addPreSteps(ArDoCoResult precomputedResults, DataRepository dataRepository) {
        var preprocessingData = new PreprocessingData(precomputedResults.getText());
        DataRepositoryHelper.putPreprocessingData(dataRepository, preprocessingData);
    }
}
