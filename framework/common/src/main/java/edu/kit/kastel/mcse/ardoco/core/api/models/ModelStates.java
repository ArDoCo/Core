/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class ModelStates implements PipelineStepData {
    public static final String ID = "ModelStatesData";

    private SortedMap<String, ModelExtractionState> modelExtractionStates = new TreeMap<>();
    private SortedMap<String, Model> models = new TreeMap<>();

    /**
     * Constructor to create a {@link ModelStates} object that holds all {@link ModelExtractionState}s
     */
    public ModelStates() {
        super();
    }

    /**
     * Returns the {@link ModelExtractionState} with the given id
     *
     * @param id the id
     * @return the corresponding {@link ModelExtractionState}
     */
    public ModelExtractionState getModelExtractionState(String id) {
        return modelExtractionStates.get(id);
    }

    /**
     * Adds a {@link ModelExtractionState} with the given id to the set of {@link ModelExtractionState}s
     *
     * @param id         the id
     * @param modelState the {@link ModelExtractionState}
     */
    public void addModelExtractionState(String id, ModelExtractionState modelState) {
        modelExtractionStates.put(id, modelState);
    }

    /**
     * Return the set of IDs of all {@link ModelExtractionState ModelExtractionStates} that are contained within this object.
     *
     * @return the IDs of all contained {@link ModelExtractionState ModelExtractionStates}
     */
    public SortedSet<String> extractionModelIds() {
        return new TreeSet<>(modelExtractionStates.keySet());
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
