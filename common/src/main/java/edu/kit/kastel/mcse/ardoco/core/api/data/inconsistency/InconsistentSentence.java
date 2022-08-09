package edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency;

import java.util.List;
import java.util.Locale;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;

public record InconsistentSentence(Sentence sentence, List<Inconsistency> inconsistencies) {
    // TODO

    public InconsistentSentence(Sentence sentence, Inconsistency inconsistency) {
        this(sentence, Lists.mutable.of(inconsistency));
    }

    public String getInfoString() {
        StringBuilder reasonsBuilder = new StringBuilder();
        for (var inconsistency : inconsistencies) {
            var reason = inconsistency.getReason();
            reasonsBuilder.append(reason).append("\n");
        }

        String formatString = "S%3d: \"%s\"%n\tInconsistent due to the following reasons:%n%s";
        return String.format(Locale.ENGLISH, formatString, sentence.getSentenceNumberForOutput(), sentence.getText(), reasonsBuilder);
    }

    public void addInconsistency(Inconsistency inconsistency) {
        inconsistencies.add(inconsistency);
    }

}
