/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Chooses a model based on the found occurrences of diagram elements in the available models.
 */
public class OccurrenceToDecisionInformant extends Informant {
    /**
     * The required minimum ratio of matching elements to choose a model.
     */
    /*package-private*/ static final double DEFAULT_MATCH_THRESHOLD = 0.05;
    /**
     * The maximum delta of the ratio of two models to choose both models.
     */
    /*package-private*/ static final double DEFAULT_MATCH_DELTA = 0.05;

    @Configurable
    private double matchThreshold = DEFAULT_MATCH_THRESHOLD;
    @Configurable
    private double matchDelta = DEFAULT_MATCH_DELTA;

    @Configurable
    private boolean skip = false;

    /**
     * Creates a new OccurrenceToDecisionInformant.
     *
     * @param dataRepository
     *                       The DataRepository.
     */
    public OccurrenceToDecisionInformant(DataRepository dataRepository) {
        super(OccurrenceToDecisionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        if (this.skip) {
            return;
        }

        DataRepository data = this.getDataRepository();

        ModelStates models = data.getData(ModelStates.ID, ModelStates.class).orElse(null);
        DiagramState diagram = data.getData(DiagramState.ID, DiagramStateImpl.class).orElse(null);
        DiagramMatchingModelSelectionState selection = data.getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionState.class)
                .orElse(null);

        if (models == null || diagram == null || selection == null) {
            this.logger.error("OccurrenceToDecisionInformant: Could not find all required data.");
            return;
        }

        Map<ModelType, Double> ratios = this.calculateRatios(diagram, selection);
        Set<ModelType> selectedModelTypes = this.selectModels(ratios);

        selection.setSelection(selectedModelTypes);
        selection.setSelectionExplanation(ratios);
    }

    private Map<ModelType, Double> calculateRatios(DiagramState diagramState, DiagramMatchingModelSelectionState selection) {
        Map<ModelType, Double> ratios = new LinkedHashMap<>();

        for (var modelType : selection.getAvailableModelTypes()) {
            double ratio = this.calculateRatioInModel(diagramState.getDiagram(), modelType, selection);
            ratios.put(modelType, ratio);
        }

        return ratios;
    }

    private Set<ModelType> selectModels(Map<ModelType, Double> ratios) {
        ModelType highestRatioModelType = null;
        double highestRatio = Double.MIN_VALUE;

        for (var entry : ratios.entrySet()) {
            if (highestRatioModelType == null || entry.getValue() > highestRatio) {
                highestRatioModelType = entry.getKey();
                highestRatio = entry.getValue();
            }
        }

        Set<ModelType> selectedModelTypes = new LinkedHashSet<>();

        for (var entry : ratios.entrySet()) {
            double delta = Math.abs(entry.getValue() - highestRatio);
            if (entry.getValue() >= this.matchThreshold && delta < this.matchDelta) {
                selectedModelTypes.add(entry.getKey());
            }
        }

        return selectedModelTypes;
    }

    private double calculateRatioInModel(Diagram diagram, ModelType modelType, DiagramMatchingModelSelectionState selection) {
        int numberOfElementsWithOccurrences = 0;

        for (var box : diagram.getBoxes()) {
            if (!selection.getOccurrences(box.getUUID(), modelType).isEmpty()) {
                numberOfElementsWithOccurrences++;
            }
        }

        return (double) numberOfElementsWithOccurrences / (double) diagram.getBoxes().size();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
