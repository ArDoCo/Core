/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.util.Objects;
import java.util.function.BiFunction;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * A ComparisonContext contains all information that can be used for comparing similarity between objects that occur within ArDoCo. The fields
 * {@link #firstString} and {@link #secondString} are always not null. The field {@link #lemmatize} decides whether the lemmatized version of both words should
 * be used for comparison. The field {@link #characterMatch} provides a function to determine whether two {@link UnicodeCharacter UnicodeCharacters} are
 * considered to be a match by the {@link WordSimMeasure WordSimMeasures}.
 */
public record ComparisonContext(@NotNull String firstString, @NotNull String secondString, Word firstWord, Word secondWord, boolean lemmatize,
                                @NotNull BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean> characterMatch) {

    public ComparisonContext(@NotNull String firstString, @NotNull String secondString,
            @NotNull BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean> characterMatch) {
        this(firstString, secondString, null, null, false, characterMatch);
    }

    public ComparisonContext(@NotNull String firstString, @NotNull String secondString) {
        this(firstString, secondString, null, null, false, UnicodeCharacter.EQUAL);
    }

    public ComparisonContext(@NotNull String firstString, @NotNull String secondString, boolean lemmatize) {
        this(firstString, secondString, null, null, lemmatize, UnicodeCharacter.EQUAL);
    }

    public ComparisonContext(@NotNull Word firstWord, @NotNull Word secondWord, boolean lemmatize) {
        this(firstWord.getText(), secondWord.getText(), firstWord, secondWord, lemmatize, UnicodeCharacter.EQUAL);
    }

    /**
     * Finds the most appropriate string representation by the first object in this comparison object. This method can be used as a shorthand to avoid going
     * through all variables that could possibly represent the first object.
     *
     * @return the most appropriate string presentation of the first object in this comparison
     */

    public String firstTerm() {
        return findAppropriateTerm(firstString, firstWord);
    }

    /**
     * Finds the most appropriate string representation by the second object in this comparison object. This method can be used as a shorthand to avoid going
     * through all variables that could possibly represent the second object.
     *
     * @return the most appropriate string presentation of the second object in this comparison
     */

    public String secondTerm() {
        return findAppropriateTerm(secondString, secondWord);
    }

    private String findAppropriateTerm(String string, Word word) {
        Objects.requireNonNull(string);

        if (word != null) {
            return lemmatize ? word.getLemma() : word.getText();
        } else {
            return string;
        }
    }

}
