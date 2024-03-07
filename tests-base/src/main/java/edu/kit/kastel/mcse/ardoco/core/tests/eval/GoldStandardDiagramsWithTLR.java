/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramTextTraceLink;

/**
 * This interface represents a combined gold standard. The gold standard contains the
 * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram Diagrams} and
 * {@link DiagramTextTraceLink Diagram-Sentence Trace Links} associated with the underlying
 * {@link edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject}.
 */
public interface GoldStandardDiagramsWithTLR extends GoldStandardDiagrams, GoldStandardDiagramTLR {
}
