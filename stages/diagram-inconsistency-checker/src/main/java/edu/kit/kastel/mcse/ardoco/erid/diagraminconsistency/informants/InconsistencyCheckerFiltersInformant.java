package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import java.util.Set;

import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.ConfigurationUtility;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.InitialInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class InconsistencyCheckerFiltersInformant extends Informant {
    public InconsistencyCheckerFiltersInformant(DataRepository dataRepository) {
        super(InconsistencyCheckerFiltersInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();

        //Used to filter the RIs according to the filters defined in the inconsistency checker stage
        var tempDataRepository = runInconsistencyChecker();

        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var inconsistencyStates = tempDataRepository.getData(InconsistencyStates.ID, InconsistencyStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var inconsistencyState = inconsistencyStates.getInconsistencyState(mm);
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            inconsistencyState.getRecommendedInstances().forEach(diagramInconsistencyState::addRecommendedInstance);
        }
    }

    private DataRepository runInconsistencyChecker() {
        return new AnonymousRunner(dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).orElseThrow().getProjectName()) {
            @Override
            public void initializePipelineSteps() {
                ArDoCo arDoCo = getArDoCo();
                var combinedRepository = arDoCo.getDataRepository();
                combinedRepository.addAllData(dataRepository.deepCopy());

                //Enable only the agent responsible for filtering RIs
                var config = ConfigurationUtility.enableAgents(InconsistencyChecker.class, Set.of(InitialInconsistencyAgent.class));
                arDoCo.addPipelineStep(InconsistencyChecker.get(config, combinedRepository));
            }
        }.runWithoutSaving();
    }
}
