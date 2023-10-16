package edu.kit.kastel.mcse.ardoco.tests.eval;

/**
 * This interface represents a combined gold standard. The gold standard contains the
 * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram Diagrams} and
 * {@link edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink Diagram-Sentence Trace Links} associated with the underlying
 * {@link edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject}.
 */
public interface GoldStandardDiagramsWithTLR extends GoldStandardDiagrams, GoldStandardDiagramTLR {
}
