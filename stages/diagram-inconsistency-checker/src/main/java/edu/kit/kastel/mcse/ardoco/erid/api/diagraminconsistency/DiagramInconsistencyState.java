package edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency;

import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface DiagramInconsistencyState extends PipelineStepData {
    public static final String ID = "DiagramInconsistencyState";

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
