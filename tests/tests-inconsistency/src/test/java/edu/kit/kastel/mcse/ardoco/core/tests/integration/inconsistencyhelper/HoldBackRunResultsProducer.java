/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.HoldBackElementsExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.PcmExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ArCoTLModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public class HoldBackRunResultsProducer {
    private String inputTextPath;
    private File inputModel;
    private PcmExtractor pcmExtractor;

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
    public Map<Entity, ArDoCoResult> produceHoldBackRunResults(Project project, boolean useBaselineApproach) {
        Map<Entity, ArDoCoResult> runs = new HashMap<Entity, ArDoCoResult>();
        File inputText = project.getTextFile();

        inputModel = project.getModelFile();
        inputTextPath = inputText.getPath();

        var holdBackElementsExtractor = constructHoldBackElementsExtractor();

        ArDoCo arDoCoBaseRun;
        try {
            arDoCoBaseRun = definePipelineBase(project, inputText, holdBackElementsExtractor, useBaselineApproach);
        } catch (IOException e) {
            Assertions.fail(e);
            return runs;
        }
        arDoCoBaseRun.run();
        var baseRunData = new ArDoCoResult(arDoCoBaseRun.getDataRepository());
        runs.put(null, baseRunData);

        for (int i = 0; i < holdBackElementsExtractor.numberOfActualContents(); i++) {
            holdBackElementsExtractor.setCurrentHoldBackContentIndex(i);
            var currentHoldBack = holdBackElementsExtractor.getCurrentHoldBackContent();
            var currentRun = defineArDoCoWithPreComputedData(baseRunData, holdBackElementsExtractor, useBaselineApproach);
            currentRun.run();
            runs.put(currentHoldBack, new ArDoCoResult(currentRun.getDataRepository()));
        }

        for (int i = 0; i < holdBackElementsExtractor.numberOfActualEndpoints(); i++) {
            holdBackElementsExtractor.setCurrentHoldBackEndpointIndex(i);
            var currentHoldBack = holdBackElementsExtractor.getCurrentHoldBackEndpoint();
            var currentRun = defineArDoCoWithPreComputedData(baseRunData, holdBackElementsExtractor, useBaselineApproach);
            currentRun.run();
            runs.put(currentHoldBack, new ArDoCoResult(currentRun.getDataRepository()));
        }
        return runs;
    }

    private HoldBackElementsExtractor constructHoldBackElementsExtractor() {
        pcmExtractor = new PcmExtractor(inputTextPath);
        return new HoldBackElementsExtractor(inputTextPath, pcmExtractor);
    }

    private static ArDoCo definePipelineBase(Project project, File inputText, HoldBackElementsExtractor holdBackElementsExtractor,
            boolean useInconsistencyBaseline) throws FileNotFoundException {
        ArDoCo arDoCo = new ArDoCo(project.name().toLowerCase());
        var dataRepository = arDoCo.getDataRepository();
        String text = CommonUtilities.readInputText(inputText);
        DataRepositoryHelper.putInputText(dataRepository, text);
        var additionalConfigs = project.getAdditionalConfigurations();

        arDoCo.addPipelineStep(TextPreprocessingAgent.get(additionalConfigs, dataRepository));

        addMiddleSteps(holdBackElementsExtractor, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
        }

        return arDoCo;
    }

    private static ArDoCo defineArDoCoWithPreComputedData(ArDoCoResult precomputedResults, HoldBackElementsExtractor holdBackElementsExtractor,
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

        addMiddleSteps(holdBackElementsExtractor, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(InconsistencyChecker.get(additionalConfigs, dataRepository));
        }
        return arDoCo;
    }

    private static void addMiddleSteps(HoldBackElementsExtractor holdBackElementsExtractor, ArDoCo arDoCo, DataRepository dataRepository,
            Map<String, String> additionalConfigs) {
        arDoCo.addPipelineStep(new ArCoTLModelProviderInformant(dataRepository, holdBackElementsExtractor));
        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
    }
}
