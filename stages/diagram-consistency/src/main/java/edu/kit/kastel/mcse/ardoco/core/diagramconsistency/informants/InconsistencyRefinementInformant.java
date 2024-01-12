/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement.Casing;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement.LineInversion;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement.NameExtension;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.refinement.Swap;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * This informant refines existing inconsistencies into more concrete ones. These can contain multiple original
 * inconsistencies of various types.
 */
public class InconsistencyRefinementInformant extends Informant {
    /**
     * Creates a new InconsistencyRefinementInformant.
     *
     * @param data
     *             The DataRepository.
     */
    public InconsistencyRefinementInformant(DataRepository data) {
        super(InconsistencyRefinementInformant.class.getSimpleName(), data);
    }

    private static Map<String, String> getBoxNames(DiagramState diagramState) {
        Diagram diagram = diagramState.getDiagram();
        return diagram.getBoxes().stream().collect(Collectors.toMap(Box::getUUID, DiagramUtility::getBoxText));
    }

    private static Map<String, String> getEntityNames(ModelStates models, ModelType selectedModelType) {
        Model model = models.getModel(selectedModelType.getModelId());
        Map<String, Entity> entities = Extractions.extractEntitiesFromModel(model);

        Map<String, String> entityIdToName = new LinkedHashMap<>();
        for (var entity : entities.entrySet()) {
            entityIdToName.put(entity.getKey(), entity.getValue().getName());
        }

        return entityIdToName;
    }

    @Override
    public void process() {
        DataRepository data = this.getDataRepository();

        ModelStates models = data.getData(ModelStates.ID, ModelStates.class).orElse(null);
        DiagramState diagram = data.getData(DiagramState.ID, DiagramStateImpl.class).orElse(null);
        DiagramMatchingModelSelectionState selection = data.getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionStateImpl.class)
                .orElse(null);
        DiagramModelInconsistencyState inconsistencyState = data.getData(DiagramModelInconsistencyState.ID, DiagramModelInconsistencyState.class).orElse(null);

        if (models == null || diagram == null || selection == null || inconsistencyState == null) {
            this.logger.error("InconsistencyRefinementInformant: Could not find all required data.");
            return;
        }

        for (var selectedModelType : selection.getSelection()) {
            List<Inconsistency<String, String>> inconsistencies = new ArrayList<>(inconsistencyState.getInconsistencies(selectedModelType));

            Map<String, String> boxIdToName = getBoxNames(diagram);
            Map<String, String> entityIdToName = getEntityNames(models, selectedModelType);

            List<UnaryOperator<List<Inconsistency<String, String>>>> rules = List.of(LineInversion::discover, list -> NameExtension.discover(list,
                    boxIdToName::get, entityIdToName::get), Casing::discover, Swap::discover);

            for (var refinementRule : rules) {
                inconsistencies = refinementRule.apply(inconsistencies);
            }

            inconsistencyState.setExtendedInconsistencies(selectedModelType, inconsistencies);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
