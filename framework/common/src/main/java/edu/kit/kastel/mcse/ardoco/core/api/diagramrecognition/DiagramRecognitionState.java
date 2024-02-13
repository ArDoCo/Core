/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * The diagram recognition state contains {@link Diagram Diagrams} that are detected in files of the project.
 */
public interface DiagramRecognitionState extends PipelineStepData {
    String ID = "DiagramRecognition";

    /**
     * Add a new diagram to the state that has not been processed yet.
     *
     * @param diagram the diagram
     */
    void addUnprocessedDiagram(Diagram diagram);

    /**
     * {@return an immutable list of unprocessed diagrams}
     */
    List<Diagram> getUnprocessedDiagrams();

    /**
     * Removes a diagram from the unprocessed diagrams
     *
     * @param diagram the diagram to remove
     * @return true, if the diagram was removed successfully
     */
    boolean removeUnprocessedDiagram(Diagram diagram);

    /**
     * Add a new diagram to the state.
     *
     * @param diagram the diagram
     */
    void addDiagram(Diagram diagram);

    /**
     * Get a list of all recognized diagrams.
     *
     * @return all recognized diagrams
     */
    List<Diagram> getDiagrams();

    /**
     * Add a disambiguation to the state.
     *
     * @param disambiguation the disambiguation
     * @return Whether it was added successfully
     */
    boolean addDisambiguation(Disambiguation disambiguation);

    /**
     * {@return a list of disambiguations that were discovered during the stage}
     */
    List<Disambiguation> getDisambiguations();
}
