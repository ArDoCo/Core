/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency;

/**
 * This informant punishes {@link MDEInconsistency MDEInconsistencies} by lowering the confidence of the recommended instance and rewards consistency by
 * increasing the confidence. The adjustment is based on a configurable {@link #maximumReward} and {@link #maximumPunishment}.
 */
public class InfluenceByInconsistenciesInformant extends Informant {

    @Configurable
    private double maximumReward = .55;

    @Configurable
    private double maximumPunishment = -10d;

    /**
     * Creates a new informant that acts on the specified data repository
     *
     * @param dataRepository the data repository
     */
    public InfluenceByInconsistenciesInformant(DataRepository dataRepository) {
        super(InfluenceByInconsistenciesInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var dataRepository = getDataRepository();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var metamodels = Metamodel.values();
        for (var mm : metamodels) {
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);
            var allRecommendedInstances = recommendationStates.getRecommendationState(mm).getRecommendedInstances();
            var coveredRecommendedInstances = allRecommendedInstances.stream()
                    .filter(ri -> !diagramConnectionState.getLinksBetweenDeAndRi(ri).isEmpty())
                    .distinct()
                    .toList();
            var mdeInconsistencies = diagramInconsistencyState.getInconsistencies(MDEInconsistency.class);
            var linksBetweenDeAndRi = allRecommendedInstances.stream()
                    .flatMap(ri -> diagramConnectionState.getLinksBetweenDeAndRi(ri).stream())
                    .collect(Collectors.toSet());
            var coverage = coveredRecommendedInstances.size() / (double) allRecommendedInstances.size();
            logger.info("Recommended Instances coverage {}%, Covered RIs {}, All RIs {}", coverage * 100, coveredRecommendedInstances.size(),
                    allRecommendedInstances.size());
            punishInconsistency(coverage, mdeInconsistencies);
            rewardConsistency(coverage, linksBetweenDeAndRi);
        }
    }

    private void punishInconsistency(double coverage, Set<MDEInconsistency> mdeInconsistencySet) {
        mdeInconsistencySet.forEach(mde -> {
            var old = mde.recommendedInstance().getProbability();
            //Punish more if coverage is high
            var probability = clamp(old + maximumPunishment * coverage, 0.0, 1.0);
            mde.recommendedInstance().addProbability(this, probability);
        });
    }

    private void rewardConsistency(double coverage, Set<LinkBetweenDeAndRi> linkBetweenDeAndRiSet) {
        linkBetweenDeAndRiSet.forEach(dl -> {
            var old = dl.getRecommendedInstance().getProbability();
            //Reward more if coverage is low
            var probability = clamp(old + maximumReward * (1.0 - coverage), 0.0, 1.0);
            dl.getRecommendedInstance().addProbability(this, probability);
        });
    }

    private double clamp(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }
}
