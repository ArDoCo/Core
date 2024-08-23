/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper;

import java.util.Comparator;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;

/**
 * Represents a simple trace link by the id of the model and number of the sentence involved.
 */
public record ModelElementSentenceLink(String modelElementId, int sentenceNumber) implements Comparable<ModelElementSentenceLink> {

    public ModelElementSentenceLink(SadSamTraceLink traceLink) {
        this(traceLink.getModelElementUid(), traceLink.getSentenceNumber());
    }

    @Override
    public int compareTo(ModelElementSentenceLink o) {
        return Comparator.comparing(ModelElementSentenceLink::modelElementId).thenComparing(ModelElementSentenceLink::sentenceNumber).compare(this, o);
    }

}
