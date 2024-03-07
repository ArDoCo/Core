/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramGoldStandardTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramTextTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.id.tests.eval.results.ExpectedResults;

/**
 * This interface represents an interface which contains a set of {@link DiagramTextTraceLink Diagram-Sentence Trace Links} for the underlying
 * {@link GoldStandardProject}.
 */
public interface GoldStandardDiagramTLR extends GoldStandardProject {
    /**
     * {@return the set of diagram-sentence trace links} The sentence numbers from the gold standard are resolved to full sentences using the provided list of
     * sentences.
     *
     * @param sentences sentences of the text
     */
    Set<DiagramGoldStandardTraceLink> getDiagramTraceLinks(List<Sentence> sentences);

    /**
     * {@return a map of diagram-sentence trace links to their corresponding trace type} The sentence numbers from the gold standard are resolved to full
     * sentences using the provided list of sentences.
     *
     * @param sentences sentences of the text
     */
    Map<TraceType, List<DiagramGoldStandardTraceLink>> getDiagramTraceLinksAsMap(List<Sentence> sentences);

    /**
     * Returns the expected results from the diagram-sentence traceability link recovery using gold standard diagrams
     *
     * @return the expectedDiagramTraceLinkResults
     */
    ExpectedResults getExpectedDiagramSentenceTlrResultsWithMock();

    /**
     * Returns the expected results from the diagram-sentence traceability link recovery
     *
     * @return the expectedDiagramTraceLinkResults
     */
    ExpectedResults getExpectedDiagramSentenceTlrResults();

    /**
     * {@return the expected results for MME detection}
     */
    ExpectedResults getExpectedMMEResults();

    /**
     * {@return the expected results for MME detection using the gold standard diagrams}
     */
    ExpectedResults getExpectedMMEResultsWithMock();

    /**
     * {@return the expected SAD-SAM trace link results}
     */
    ExpectedResults getExpectedSadSamResults();

    /**
     * {@return the expected SAD-SAM trace link results using the gold standard diagrams}
     */
    ExpectedResults getExpectedSadSamResultsWithMock();
}
