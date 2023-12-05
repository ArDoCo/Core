/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class ModelStates implements PipelineStepData {
    public static final String ID = "ModelStatesData";

    private SortedMap<String, Model> models = new TreeMap<>();
    private SortedMap<String, LegacyModelExtractionState> legacyModels = new TreeMap<>();

    /**
     * Constructor to create a {@link ModelStates} object that holds all {@link LegacyModelExtractionState}s
     */
    public ModelStates() {
        super();
    }

    /**
     * Returns the {@link LegacyModelExtractionState} with the given id
     *
     * @param id the id
     * @return the corresponding {@link LegacyModelExtractionState}
     * @deprecated use {@link #getModel(String)} instead
     */
    @Deprecated
    public LegacyModelExtractionState getModelExtractionState(String id) {
        if (legacyModels.containsKey(id))
            return legacyModels.get(id);

        var model = models.get(id);
        if (model == null)
            return null;

        var legacyModel = switch (model) {
        case ArchitectureModel am -> new LegacyModelExtractionStateByArCoTL(am);
        case CodeModel cm -> new LegacyModelExtractionStateByArCoTL(cm);
        };

        legacyModels.put(id, legacyModel);
        return legacyModel;
    }

    /**
     * Return the set of IDs of all {@link Model Models} that are contained within this object.
     *
     * @return the IDs of all contained {@link Model Models}
     */
    public SortedSet<String> modelIds() {
        return new TreeSet<>(models.keySet());
    }

    /**
     * Adds a {@link Model} with the given id to the set of {@link Model Models}
     *
     * @param id    the id
     * @param model the {@link Model}
     */
    public void addModel(String id, Model model) {
        models.put(id, model);
    }

    /**
     * Returns the {@link Model} with the given id
     *
     * @param id the id
     * @return the corresponding {@link Model}
     */
    public Model getModel(String id) {
        return models.get(id);
    }

}
