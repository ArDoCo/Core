/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement.Group;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * This informant groups inconsistencies into groups of related ones.
 */
public class InconsistencyGroupingInformant extends Informant {
    /**
     * Creates a new InconsistencyGroupingInformant.
     *
     * @param data
     *             The DataRepository.
     */
    public InconsistencyGroupingInformant(DataRepository data) {
        super(InconsistencyGroupingInformant.class.getSimpleName(), data);
    }

    @Override
    public void process() {
        DataRepository data = this.getDataRepository();

        DiagramMatchingModelSelectionState selection = data.getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionStateImpl.class)
                .orElse(null);
        DiagramModelInconsistencyState inconsistencyState = data.getData(DiagramModelInconsistencyState.ID, DiagramModelInconsistencyState.class).orElse(null);

        if (selection == null || inconsistencyState == null) {
            this.logger.error("InconsistencyGroupingInformant: Could not find all required data.");
            return;
        }

        for (var selectedModelType : selection.getSelection()) {
            List<Inconsistency<String, String>> inconsistencies = inconsistencyState.getExtendedInconsistencies(selectedModelType);

            Map<String, List<Inconsistency<String, String>>> groups = new LinkedHashMap<>();
            List<Inconsistency<String, String>> ungrouped = new ArrayList<>();

            for (var inconsistency : inconsistencies) {
                if (inconsistency.getBox() != null) {
                    groups.computeIfAbsent(inconsistency.getBox(), k -> new ArrayList<>()).add(inconsistency);
                } else {
                    ungrouped.add(inconsistency);
                }
            }

            List<Inconsistency<String, String>> newInconsistencies = new ArrayList<>(ungrouped);
            for (var group : groups.entrySet()) {
                if (group.getValue().size() > 1) {
                    newInconsistencies.add(new Group<>(group.getKey(), group.getValue()));
                } else {
                    newInconsistencies.add(group.getValue().get(0));
                }
            }

            inconsistencyState.setExtendedInconsistencies(selectedModelType, newInconsistencies);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
