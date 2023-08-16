package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency;

public class InfluenceByInconsistenciesInformant extends Informant {
    private final static Logger logger = LoggerFactory.getLogger(InfluenceByInconsistenciesInformant.class);

    @Configurable
    private double maximumReward = 0.25;

    public InfluenceByInconsistenciesInformant(DataRepository dataRepository) {
        super(InfluenceByInconsistenciesInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);
            var allRecommendedInstances = recommendationStates.getRecommendationState(mm).getRecommendedInstances();
            var coveredRecommendedInstances = allRecommendedInstances.stream()
                    .filter(ri -> !diagramConnectionState.getLinksBetweenDeAndRi(ri).isEmpty())
                    .distinct()
                    .toList();
            var mdeInconsistencies = diagramInconsistencyState.getInconsistencies(MDEInconsistency.class);
            var linksBetweenDeAndRi = allRecommendedInstances.stream().flatMap(ri -> diagramConnectionState.getLinksBetweenDeAndRi(ri).stream()).collect(Collectors.toSet());
            var coverage = coveredRecommendedInstances.size() / (double) allRecommendedInstances.size();
            logger.info("Recommended Instances coverage {}%, Covered RIs {}, All RIs {}", coverage * 100, coveredRecommendedInstances.size(),
                    allRecommendedInstances.size());
            punishInconsistency(coverage, mdeInconsistencies);
            rewardConsistency(coverage, linksBetweenDeAndRi);
        }
    }

    public void punishInconsistency(double coverage, Set<MDEInconsistency> mdeInconsistencySet) {
        mdeInconsistencySet.forEach(mde -> {
            var old = mde.recommendedInstance().getProbability();
            //Punish more if mdes are rare
            var probability = clamp(old * (1.0 - coverage), 0.0, 1.0);
            mde.recommendedInstance().addProbability(this, probability);
        });
    }

    public void rewardConsistency(double coverage, Set<LinkBetweenDeAndRi> linkBetweenDeAndRiSet) {
        linkBetweenDeAndRiSet.forEach(dl -> {
            var old = dl.getRecommendedInstance().getProbability();
            //Reward more if mdes are rare
            var probability = clamp(old + maximumReward * (1.0 - coverage), 0.0, 1.0);
            dl.getRecommendedInstance().addProbability(this, probability);
        });
    }

    private double clamp(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }
}
