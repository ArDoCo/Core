package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyState;

public class DiagramInconsistencyStateImpl implements DiagramInconsistencyState {
    private final Set<RecommendedInstance> recommendedInstances = new HashSet<>();
    private final Set<Inconsistency> inconsistencies = new HashSet<>();

    @Override
    public Set<RecommendedInstance> getRecommendedInstances() {
        return Set.copyOf(recommendedInstances);
    }

    @Override
    public boolean addRecommendedInstance(RecommendedInstance recommendedInstance) {
        return recommendedInstances.add(recommendedInstance);
    }

    @Override
    public <T extends Inconsistency> Set<T> getInconsistencies(Class<T> type) {
        return inconsistencies.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Inconsistency> getInconsistencies() {
        return Set.copyOf(inconsistencies);
    }

    @Override
    public boolean addInconsistency(Inconsistency inconsistency) {
        return inconsistencies.add(inconsistency);
    }
}
