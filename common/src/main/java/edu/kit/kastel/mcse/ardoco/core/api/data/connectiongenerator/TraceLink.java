/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

/**
 * Represents a tracelink. Is a convenience data class that takes the necessary info from {@link InstanceLink} and the
 * specific {@link ModelInstance} and {@link Word} that are used in this tracelink.
 *
 * @author Jan Keim
 */
public class TraceLink {
    private final InstanceLink instanceLink;
    private final ModelInstance modelInstance;
    private final Word word;

    /**
     * Create a tracelink based on a {@link InstanceLink} and a concrete {@link ModelInstance} along with a concrete
     * {@link Word}.
     *
     * @param instanceLink  InstanceLink of this tracelink
     * @param modelInstance modelInstance that the tracelink points to
     * @param word          word that the tracelink points to
     */
    public TraceLink(InstanceLink instanceLink, ModelInstance modelInstance, Word word) {
        this.instanceLink = instanceLink;
        this.modelInstance = modelInstance;
        this.word = word;
    }

    /**
     * Get the sentence number of the word that the tracelink is based on.
     *
     * @return sentence number of the word that the tracelink is based on.
     */
    public int getSentenceNumber() {
        return word.getSentenceNo();
    }

    /**
     * Get the UID of the model element that the tracelink is based on.
     *
     * @return Uid of the model element that the tracelink is based on.
     */
    public String getModelElementUid() {
        return modelInstance.getUid();
    }

    /**
     * Get the {@link InstanceLink} that the tracelink is based on.
     *
     * @return {@link InstanceLink} that the tracelink is based on.
     */
    public InstanceLink getInstanceLink() {
        return instanceLink;
    }

    /**
     * Get the probability/confidence of this tracelink
     *
     * @return probability/confidence of this tracelink
     */
    public double getProbability() {
        return instanceLink.getProbability();
    }

    /**
     * See {@link Object#equals(Object)}. Uses the Uid of the model element and the sentence number of the word
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TraceLink other) {
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

}
