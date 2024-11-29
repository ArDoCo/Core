/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.TextEntity;

public final class SentenceEntity extends TextEntity {

    private final Sentence sentence;

    public SentenceEntity(Sentence sentence) {
        super(sentence.getText(), String.valueOf(sentence.getSentenceNumber()));
        this.sentence = sentence;
    }

    public Sentence getSentence() {
        return sentence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SentenceEntity that = (SentenceEntity) o;
        return Objects.equals(sentence, that.sentence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentence);
    }
}
