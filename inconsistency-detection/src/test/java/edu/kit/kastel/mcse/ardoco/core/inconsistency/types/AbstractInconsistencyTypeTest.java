/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.*;

/**
 *
 */
public abstract class AbstractInconsistencyTypeTest {

    protected abstract Inconsistency getInconsistency();

    protected abstract String getTypeString();

    protected abstract String getReasonString();

    protected abstract Inconsistency getUnequalInconsistency();

    protected abstract Inconsistency getEqualInconsistency();

    protected abstract String[] getFileOutputEntry();

    @Test
    void getTypeTest() {
        Assertions.assertEquals(getTypeString(), getInconsistency().getType());
    }

    @Disabled("Disabled for now as the (expected) values might change regularly")
    @Test
    void toFileOutputTest() {
        var fileOutput = getInconsistency().toFileOutput();
        var entry = fileOutput.getAny();
        var expectedEntry = getFileOutputEntry();
        Assertions.assertAll(//
                () -> Assertions.assertEquals(1, fileOutput.size()), //
                () -> Assertions.assertEquals(expectedEntry.length, entry.length), //
                () -> {
                    for (var i = 0; i < expectedEntry.length; i++) {
                        Assertions.assertEquals(expectedEntry[i], entry[i]);
                    }
                });
    }

    @Disabled("Disabled for now as the (expected) values might change regularly")
    @Test
    void getReasonTest() {
        var expectedReason = getReasonString();
        var actualReason = getInconsistency().getReason();
        Assertions.assertEquals(expectedReason, actualReason);
    }

    @Test
    void equalsTest() {
        var otherEqualInconsistency = getEqualInconsistency();
        var otherUnequalInconsistency = getUnequalInconsistency();
        var equality = getInconsistency().equals(otherEqualInconsistency);
        var inequality = getInconsistency().equals(otherUnequalInconsistency);
        Assertions.assertAll(//
                () -> Assertions.assertTrue(equality), //
                () -> Assertions.assertFalse(inequality));
    }

    protected static class DummyWord implements Word {

        @Override
        public int getSentenceNo() {
            return 0;
        }

        @Override
        public Sentence getSentence() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Phrase getPhrase() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText() {
            return "text";
        }

        @Override
        public POSTag getPosTag() {
            return POSTag.NOUN;
        }

        @Override
        public Word getPreWord() {
            return null;
        }

        @Override
        public Word getNextWord() {
            return null;
        }

        @Override
        public int getPosition() {
            return 0;
        }

        @Override
        public String getLemma() {
            return "text";
        }

        @Override
        public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
            return Lists.immutable.empty();
        }

        @Override
        public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
            return Lists.immutable.empty();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPosition(), getSentenceNo(), getText());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DummyWord other)) {
                return false;
            }

            return getPosition() == other.getPosition() && getSentenceNo() == other.getSentenceNo() && Objects.equals(getText(), other.getText());
        }
    }

}
