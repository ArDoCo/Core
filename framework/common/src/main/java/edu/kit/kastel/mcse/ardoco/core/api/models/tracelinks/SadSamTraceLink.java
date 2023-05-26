/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * Represents a trace link. This is a convenience data class that takes the necessary info from {@link InstanceLink} and
 * the specific {@link ModelInstance} and {@link Word} that are used in this trace link.
 */
public class SadSamTraceLink extends TraceLink implements Comparable<SadSamTraceLink> {
    private final InstanceLink instanceLink;
    private final Word word;

    /**
     * Create a trace link based on a {@link InstanceLink} and a concrete {@link ModelInstance} along with a concrete
     * {@link Word}.
     *
     * @param instanceLink InstanceLink of this trace link
     * @param word         word that the trace link points to
     */
    public SadSamTraceLink(InstanceLink instanceLink, Word word) {
        super(new EndpointTuple(instanceLink.getTextualInstance(), instanceLink.getModelInstance()));
        this.instanceLink = instanceLink;
        this.word = word;
    }

    /**
     * Get the sentence number of the word that the trace link is based on.
     *
     * @return sentence number of the word that the trace link is based on.
     */
    public int getSentenceNumber() {
        return word.getSentenceNo();
    }

    /**
     * Returns the sentence of the word that the trace link is based on.
     *
     * @return the sentence of the word that the trace link is based on.
     */
    public Sentence getSentence() {
        return word.getSentence();
    }

    /**
     * Get the UID of the model element that the trace link is based on.
     *
     * @return Uid of the model element that the trace link is based on.
     */
    public String getModelElementUid() {
        return instanceLink.getModelInstance().getUid();
    }

    /**
     * Get the {@link InstanceLink} that the trace link is based on.
     *
     * @return {@link InstanceLink} that the trace link is based on.
     */
    public InstanceLink getInstanceLink() {
        return instanceLink;
    }

    /**
     * Get the probability/confidence of this trace link
     *
     * @return probability/confidence of this trace link
     */
    public double getConfidence() {
        return instanceLink.getConfidence();
    }

    /**
     * See {@link Object#equals(Object)}. Uses the Uid of the model element and the sentence number of the word
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SadSamTraceLink other) {
            var otherId = other.getModelElementUid();
            var otherSentenceNo = other.getSentenceNumber();
            return getModelElementUid().equals(otherId) && getSentenceNumber() == otherSentenceNo;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModelElementUid(), getSentenceNumber());
    }

    @Override
    public int compareTo(SadSamTraceLink o) {
        return Integer.compare(this.getSentenceNumber(), o.getSentenceNumber());
    }
}
