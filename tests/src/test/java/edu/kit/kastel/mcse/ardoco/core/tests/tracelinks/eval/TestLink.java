/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval;

import java.util.Comparator;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.TraceLink;

public record TestLink(String modelId, int sentenceNr) implements Comparable<TestLink> {

    public TestLink(TraceLink traceLink) {
        this(traceLink.getModelElementUid(), traceLink.getSentenceNumber());
    }

    @Override
    public int compareTo(TestLink o) {
        return Comparator.comparing(TestLink::modelId).thenComparing(TestLink::sentenceNr).compare(this, o);
    }

}