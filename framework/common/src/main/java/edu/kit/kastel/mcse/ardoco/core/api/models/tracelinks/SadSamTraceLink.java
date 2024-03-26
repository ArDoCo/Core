/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * Represents a trace link. This is a convenience data class that takes the necessary info from {@link InstanceLink} and
 * the specific {@link ModelInstance} and {@link Word} that are used in this trace link.
 */
public class SadSamTraceLink extends TraceLink<SentenceEntity, ArchitectureEntity> {

    private final Sentence sentence;
    private final ModelInstance modelInstance;

    /**
     * Create a trace link based on a {@link Sentence} and a concrete {@link ModelInstance} .
     */
    public SadSamTraceLink(Sentence sentence, ModelInstance modelInstance) {
        super(new SentenceEntity(sentence), modelInstance);
        this.sentence = sentence;
        this.modelInstance = modelInstance;
    }

    /**
     * Get the sentence number of the word that the trace link is based on.
     *
     * @return sentence number of the word that the trace link is based on.
     */
    public int getSentenceNumber() {
        return sentence.getSentenceNumber();
    }

    /**
     * Returns the sentence of the word that the trace link is based on.
     *
     * @return the sentence of the word that the trace link is based on.
     */
    public Sentence getSentence() {
        return sentence;
    }

    /**
     * Get the UID of the model element that the trace link is based on.
     *
     * @return Uid of the model element that the trace link is based on.
     */
    public String getModelElementUid() {
        return modelInstance.getUid();
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

}
