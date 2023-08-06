package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import java.util.Set;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.ConfigurationUtility;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.InitialInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;

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
        var clonedInconsistencyStates = DataRepositoryHelper.getInconsistencyStates(tempDataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var clonedInconsistencyState = clonedInconsistencyStates.getInconsistencyState(mm);
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            var originalRIs = recommendationStates.getRecommendationState(mm).getRecommendedInstances();
            //Restore referential integrity by finding the original ri
            clonedInconsistencyState.getRecommendedInstances()
                    .forEach(clonedRI -> diagramInconsistencyState.addRecommendedInstance(getOriginalRI(originalRIs, clonedRI)));
            //Assert referential integrity
            assert clonedInconsistencyState.getRecommendedInstances().allSatisfy(clonedRI -> {
                var original = getOriginalRI(originalRIs, clonedRI);
                return dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class)
                        .orElseThrow()
                        .getDiagramConnectionState(mm)
                        .getDiagramLinks(original)
                        .stream()
                        .findFirst()
                        .map(l -> l.getRecommendedInstance() == original)
                        .orElse(true);
            });
        }
    }

    private RecommendedInstance getOriginalRI(ImmutableList<RecommendedInstance> originalRIs, RecommendedInstance clone) {
        return originalRIs.stream().filter(clone::equals).findFirst().orElseThrow();
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
