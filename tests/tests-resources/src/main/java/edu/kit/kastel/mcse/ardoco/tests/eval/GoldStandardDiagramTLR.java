package edu.kit.kastel.mcse.ardoco.tests.eval;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GoldStandardDiagramTLR extends GoldStandardProject {
    Set<DiaGSTraceLink> getDiagramTraceLinks(List<Sentence> sentences);
    Map<TraceType, List<DiaGSTraceLink>> getDiagramTraceLinksAsMap(List<Sentence> sentences);
    ExpectedResults getExpectedDiagramTraceLinkResults();
}
