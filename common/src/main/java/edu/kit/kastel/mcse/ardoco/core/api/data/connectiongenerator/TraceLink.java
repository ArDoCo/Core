/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

/**
 * Represents a tracelink. Is a convenience data class that takes the necessary info from {@link IInstanceLink} and the
 * specific {@link IModelInstance} and {@link IWord} that are used in this tracelink.
 *
 * @author Jan Keim
 */
public class TraceLink {
    private final IInstanceLink instanceLink;
    private final IModelInstance modelInstance;
    private final IWord word;

    /**
     * Create a tracelink based on a {@link IInstanceLink} and a concrete {@link IModelInstance} along with a concrete
     * {@link IWord}.
     *
     * @param instanceLink  InstanceLink of this tracelink
     * @param modelInstance modelInstance that the tracelink points to
     * @param word          word that the tracelink points to
     */
    public TraceLink(IInstanceLink instanceLink, IModelInstance modelInstance, IWord word) {
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
     * Get the {@link IInstanceLink} that the tracelink is based on.
     *
     * @return {@link IInstanceLink} that the tracelink is based on.
     */
    public IInstanceLink getInstanceLink() {
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
