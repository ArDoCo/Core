package edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency;

import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface DiagramInconsistencyState extends PipelineStepData {
    public static final String ID = "DiagramInconsistencyState";

    /**
     * {@return the filtered set of recommended instances used for diagram inconsistency detection}
     */
    public Set<RecommendedInstance> getRecommendedInstances();

    /**
     * Adds a recommended instance used for diagram inconsistency detection
     *
     * @param recommendedInstance recommended instance to add
     * @return true, if the recommended instance was added to the state, false else
     */
    public boolean addRecommendedInstance(RecommendedInstance recommendedInstance);

    /**
     * @param type inconsistency type {@return the set of inconsistencies discovered by this stage of the given type}
     */
    public <T extends Inconsistency> Set<T> getInconsistencies(Class<T> type);

    /**
     * {@return the set of inconsistencies discovered by this stage}
     */
    public Set<Inconsistency> getInconsistencies();

    /**
     * Adds an inconsistency
     *
     * @param inconsistency instance
     * @return true, if the inconsistency was added to the state, false else
     */
    public boolean addInconsistency(Inconsistency inconsistency);
}
