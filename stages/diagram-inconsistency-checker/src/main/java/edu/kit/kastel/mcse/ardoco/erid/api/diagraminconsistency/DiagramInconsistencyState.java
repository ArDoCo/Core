/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency;

import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * This state holds the diagram element {@link Inconsistency Inconsistencies}
 * {@link edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency MDEInconsistency} and
 * {@link edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MTDEInconsistency MTDEInconsistency}.
 */
public interface DiagramInconsistencyState extends PipelineStepData {
    String ID = "DiagramInconsistencyState";

    /**
     * {@return the inconsistencies of the given type}
     *
     * @param type inconsistency type
     */
    <T extends Inconsistency> Set<T> getInconsistencies(Class<T> type);

    /**
     * {@return the set of inconsistencies discovered by this stage}
     */
    Set<Inconsistency> getInconsistencies();

    /**
     * Adds an inconsistency
     *
     * @param inconsistency instance
     * @return true, if the inconsistency was added to the state, false else
     */
    boolean addInconsistency(Inconsistency inconsistency);
}
