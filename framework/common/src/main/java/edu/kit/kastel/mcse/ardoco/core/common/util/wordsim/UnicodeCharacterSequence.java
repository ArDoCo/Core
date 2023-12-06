/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.util.Objects;
import java.util.stream.IntStream;

import org.eclipse.collections.api.list.ImmutableList;
import org.jetbrains.annotations.NotNull;

/**
 * {@link UnicodeCharacter} equivalent of {@link CharSequence}.
 *
 * @param characters list containing the sequence
 */
public record UnicodeCharacterSequence(ImmutableList<UnicodeCharacter> characters) {
    public static UnicodeCharacterSequence valueOf(String input) {
        return new UnicodeCharacterSequence(UnicodeCharacter.from(input));
    }

    public UnicodeCharacter charAt(int index) {
        return characters.get(index);
    }

    public IntStream codePoints() {
        return characters.stream().mapToInt(UnicodeCharacter::getCodePoint);
    }

    public int length() {
        return characters.size();
    }

    public UnicodeCharacterSequence subSequence(int start, int end) {
        return new UnicodeCharacterSequence(characters.subList(start, end));
    }

    @Override
    public String toString() {
        return UnicodeCharacter.toString(characters.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof UnicodeCharacterSequence oth)
            return characters.containsAll(oth.characters.toList()) && characters.size() == oth.characters.size();
        return false;
    }

    /**
     * {@return whether all characters of both sequence match}
     *
     * @param oth            the other UnicodeCharacterSequence
     * @param characterMatch the function applied to determine if two UnicodeCharacters match
     */
    public boolean match(@NotNull UnicodeCharacterSequence oth, @NotNull UnicodeCharacterMatchFunctions characterMatch) {
        if (this == oth)
            return true;
        if (length() != oth.length())
            return false;
        return characters.zip(oth.characters).allSatisfy(p -> characterMatch.apply(p.getOne(), p.getTwo()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(characters);
    }
}
