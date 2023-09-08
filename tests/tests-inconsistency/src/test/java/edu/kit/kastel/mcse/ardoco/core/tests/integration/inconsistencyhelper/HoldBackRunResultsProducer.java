/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.PcmXmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class HoldBackRunResultsProducer implements Serializable {
    protected File inputText;
    protected File inputModel;
    protected SortedMap<String, String> additionalConfigs;
    protected PcmXmlModelConnector pcmModel;

    public HoldBackRunResultsProducer() {
        super();
    }

    /**
     * Runs ArDoCo or the ArDoCo-backed baseline approach multiple times to produce results. The first run calls ArDoCo normally, in further runs
     * one element is
     * held back each time (so that each element was held back once). This way, we can simulate missing elements.
     *
     * @param goldStandardProject the project that should be run
     * @param useBaselineApproach set to true if the baseline approach should be used instead of ArDoCo
     * @return a map containing the mapping from ModelElement that was held back to the DataStructure that was produced when running ArDoCo without
     * the
     * ModelElement
     */
    public Map<ModelInstance, ArDoCoResult> produceHoldBackRunResults(GoldStandardProject goldStandardProject, boolean useBaselineApproach) {
        Map<ModelInstance, ArDoCoResult> runs = new HashMap<>();
        inputModel = goldStandardProject.getModelFile();
        inputText = goldStandardProject.getTextFile();
        additionalConfigs = goldStandardProject.getAdditionalConfigurations();

        var holdElementsBackModelConnector = constructHoldElementsBackModelConnector();

        var preRunDataRepository = runShared(goldStandardProject);

        var baseRunData = new ArDoCoResult(runUnshared(goldStandardProject, holdElementsBackModelConnector, preRunDataRepository.deepCopy(),
                useBaselineApproach));
        runs.put(null, baseRunData);

        for (int i = 0; i < holdElementsBackModelConnector.numberOfActualInstances(); i++) {
            holdElementsBackModelConnector.setCurrentHoldBackIndex(i);
            var currentHoldBack = holdElementsBackModelConnector.getCurrentHoldBack();
            var currentRunData = runUnshared(goldStandardProject, holdElementsBackModelConnector, preRunDataRepository.deepCopy(),
                    useBaselineApproach);
            var result = new ArDoCoResult(currentRunData);
            runs.put(currentHoldBack, result);
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

    protected DataRepository runShared(GoldStandardProject goldStandardProject) {
        return new AnonymousRunner(goldStandardProject.getProjectName()) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                var text = CommonUtilities.readInputText(inputText);
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }
                DataRepositoryHelper.putInputText(dataRepository, text);
                pipelineSteps.add(TextPreprocessingAgent.get(additionalConfigs, dataRepository));
                pipelineSteps.add(TextExtraction.get(additionalConfigs, dataRepository));

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    protected DataRepository runUnshared(GoldStandardProject goldStandardProject, HoldElementsBackModelConnector holdElementsBackModelConnector,
                                         DataRepository preRunDataRepository,
                                         boolean useInconsistencyBaseline) {
        return new AnonymousRunner(goldStandardProject.getProjectName(), preRunDataRepository) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                pipelineSteps.add(new ModelProviderInformant(dataRepository, holdElementsBackModelConnector));
                pipelineSteps.add(RecommendationGenerator.get(additionalConfigs, dataRepository));
                pipelineSteps.add(ConnectionGenerator.get(additionalConfigs, dataRepository));

                if (useInconsistencyBaseline) {
                    pipelineSteps.add(new InconsistencyBaseline(dataRepository));
                } else {
                    pipelineSteps.add(InconsistencyChecker.get(additionalConfigs, dataRepository));
                }

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }
}
