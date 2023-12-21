/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;

/**
 * Implementation of {@link DiagramModelInconsistencyState}.
 */
public class DiagramInconsistencyStateImpl implements DiagramModelInconsistencyState {
    private final transient Map<ModelType, List<Inconsistency<String, String>>> inconsistencies = new LinkedHashMap<>();
    private final transient Map<ModelType, List<Inconsistency<String, String>>> extendedInconsistencies = new LinkedHashMap<>();

    @Override
    public void addInconsistency(ModelType modelType, Inconsistency<String, String> inconsistency) {
        this.inconsistencies.computeIfAbsent(modelType, k -> new ArrayList<>()).add(inconsistency);
    }

    @Override
    public List<Inconsistency<String, String>> getInconsistencies(ModelType modelType) {
        return this.inconsistencies.getOrDefault(modelType, List.of());
    }

    @Override
    public void setExtendedInconsistencies(ModelType modelType, List<Inconsistency<String, String>> inconsistencies) {
        this.extendedInconsistencies.put(modelType, inconsistencies);
    }

    @Override
    public List<Inconsistency<String, String>> getExtendedInconsistencies(ModelType modelType) {
        return this.extendedInconsistencies.getOrDefault(modelType, this.getInconsistencies(modelType));
    }
}
