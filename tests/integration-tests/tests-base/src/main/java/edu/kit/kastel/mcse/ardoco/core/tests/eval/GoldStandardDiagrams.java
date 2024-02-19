/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramGS;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;

/**
 * This interface represents a gold standard, which contains a set of {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram Diagrams} for the
 * underlying {@link GoldStandardProject}.
 */
public interface GoldStandardDiagrams extends GoldStandardProject {
    /**
     * {@return the resource name that represents the diagrams gold standard for this project}
     */
    String getDiagramsGoldStandardResourceName();

    /**
     * {@return the File that contains the gold standard for this project}
     */
    File getDiagramsGoldStandardFile();

    /**
     * {@return the set of manually extracted diagrams from the gold standard}
     */
    Set<DiagramGS> getDiagramsGoldStandard();

    /**
     * {@return the set of diagram-related resources} For example, the list contains the names of the diagram image resources.
     */
    SortedSet<String> getDiagramResourceNames();

    /**
     * {@return the list of diagram-related resources as name and file pair} For example, the list contains a pair with the name and file of each diagram image
     * resources.
     */
    List<Pair<String, File>> getDiagramData();
}
