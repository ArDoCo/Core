/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DeepCopy;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

/**
 * Produces the inconsistency detection runs. The first run uses all model elements for the baseline. For each subsequent run a single model element is removed
 * to simulate a missing model element.
 */
public class HoldBackRunResultsProducer implements Serializable {
    protected File inputText;
    protected File inputModel;
    protected SortedMap<String, String> additionalConfigs;

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
     *         ModelElement
     */
    public Map<ModelElement, ArDoCoResult> produceHoldBackRunResults(GoldStandardProject goldStandardProject, boolean useBaselineApproach) {
        Map<ModelElement, ArDoCoResult> runs = new LinkedHashMap<>();
        inputModel = goldStandardProject.getModelFile();
        inputText = goldStandardProject.getTextFile();
        additionalConfigs = goldStandardProject.getAdditionalConfigurations();

        HoldBackArCoTLModelProvider holdBackArCoTLModelProvider = new HoldBackArCoTLModelProvider(inputModel);

        var preRunDataRepository = runShared(goldStandardProject);

        var baseRunData = new ArDoCoResult(runUnshared(goldStandardProject, holdBackArCoTLModelProvider, preRunDataRepository.deepCopy(), useBaselineApproach));
        runs.put(null, baseRunData);

        for (int i = 0; i < holdBackArCoTLModelProvider.numberOfActualInstances(); i++) {
            holdBackArCoTLModelProvider.setCurrentHoldBackIndex(i);
            var currentHoldBack = holdBackArCoTLModelProvider.getCurrentHoldBack();
            var currentRunData = runUnshared(goldStandardProject, holdBackArCoTLModelProvider, preRunDataRepository.deepCopy(), useBaselineApproach);
            var result = new ArDoCoResult(currentRunData);
            runs.put(currentHoldBack, result);
        }

        return runs;
    }

    /**
     * Runs the part that is shared by all runs.
     *
     * @param goldStandardProject the current project
     * @return the data repository that is produced
     */
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

    /**
     * Runs the part that is specific to each run.
     *
     * @param goldStandardProject            the current project
     * @param holdElementsBackModelConnector the model connector with the held-back model element
     * @param preRunDataRepository           a deep copy of the data repository of the shared part
     * @param useInconsistencyBaseline       whether the inconsistency baseline is used or ArDoCo's inconsistency checker
     * @return the data repository that is produced
     */
    protected DataRepository runUnshared(GoldStandardProject goldStandardProject, HoldBackArCoTLModelProvider holdElementsBackModelConnector,
            @DeepCopy DataRepository preRunDataRepository, boolean useInconsistencyBaseline) {
        return new AnonymousRunner(goldStandardProject.getProjectName(), preRunDataRepository) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                pipelineSteps.add(holdElementsBackModelConnector.get(additionalConfigs, dataRepository));
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
