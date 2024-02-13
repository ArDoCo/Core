/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * A ComparisonContext contains all information that can be used for comparing similarity between objects that occur within ArDoCo. The fields
 * {@link #firstString} and {@link #secondString} are always not null. The field {@link #lemmatize} decides whether the lemmatized version of both words should
 * be used for comparison. The field {@link #characterMatch} provides a function to determine whether two {@link UnicodeCharacter UnicodeCharacters} are
 * considered to be a match by the {@link WordSimMeasure WordSimMeasures}.
 */
public record ComparisonContext(String firstString, String secondString, Word firstWord, Word secondWord, boolean lemmatize,
                                UnicodeCharacterMatchFunctions characterMatch) {

    /**
     * Constructs a string-based context with a given match function and no lemmatization.
     * 
     * @param firstString    the first string
     * @param secondString   the second string
     * @param characterMatch the match function
     */
    public ComparisonContext(String firstString, String secondString, UnicodeCharacterMatchFunctions characterMatch) {
        this(firstString, secondString, null, null, false, characterMatch);
    }

    /**
     * Constructs a string-based context with the default match function and no lemmatization.
     * 
     * @param firstString  the first string
     * @param secondString the second string
     */
    public ComparisonContext(String firstString, String secondString) {
        this(firstString, secondString, null, null, false, UnicodeCharacterMatchFunctions.EQUAL);
    }

    /**
     * Constructs a string-based context with the default match function.
     * 
     * @param firstString  the first string
     * @param secondString the second string
     * @param lemmatize    whether the string should be lemmatized
     */
    public ComparisonContext(String firstString, String secondString, boolean lemmatize) {
        this(firstString, secondString, null, null, lemmatize, UnicodeCharacterMatchFunctions.EQUAL);
    }

    /**
     * Constructs a word-based context with the default match function.
     * 
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param lemmatize  whether the words should be lemmatized
     */
    public ComparisonContext(Word firstWord, Word secondWord, boolean lemmatize) {
        this(firstWord.getText(), secondWord.getText(), firstWord, secondWord, lemmatize, UnicodeCharacterMatchFunctions.EQUAL);
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
