package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.Objects;

/**
 * Represents a tracelink. Is a convenience data class that takes the necessary info from {@link IInstanceLink} and the
 * specific {@link IModelInstance} and {@link IWord} that are used in this tracelink.
 *
 * @author Jan Keim
 *
 */
public class Tracelink {
    private IInstanceLink instanceLink;

    private IModelInstance modelInstance;
    private IWord word;

    public Tracelink(IInstanceLink instanceLink, IModelInstance modelInstance, IWord word) {
        this.instanceLink = instanceLink;
        this.modelInstance = modelInstance;
        this.word = word;
    }

    public int getSentenceNumber() {
        return word.getSentenceNo();
    }

    public String getModelElementUid() {
        return modelInstance.getUid();
    }

    public IInstanceLink getInstanceLink() {
        return instanceLink;
    }

    public double getProbability() {
        return instanceLink.getProbability();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tracelink other) {
            var otherId = other.getModelElementUid();
            var otherSentenceNo = other.getSentenceNumber();
            if (getModelElementUid().equals(otherId) && getSentenceNumber() == otherSentenceNo) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModelElementUid(), getSentenceNumber());
    }

}
