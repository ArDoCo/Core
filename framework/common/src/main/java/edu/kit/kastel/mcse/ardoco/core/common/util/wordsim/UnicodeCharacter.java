package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Unicode character corresponding to a particular Unicode code point. Refer to the <a href="http://www.unicode.org/glossary/">Unicode Glossary</a>
 * and the Java {@link Character} documentation for an explanation of code points. Java {@link Character} instances and the corresponding primitive {@code char}
 * can not represent all unicode characters in a single instance due to historic reasons.
 *
 * @param codePoint        the code point
 * @param unicodeCharacter the corresponding unicode character
 */
public record UnicodeCharacter(int codePoint, @NotNull String unicodeCharacter) implements Serializable {
    public static final BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean> EQUAL = UnicodeCharacter::equals;
    public static final BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean> EQUAL_OR_HOMOGLYPH = (a, b) -> EQUAL.apply(a,
            b) || ConfusablesHelper.areHomoglyphs(a, b);

    public static @NotNull ImmutableList<UnicodeCharacter> from(@NotNull String input) {
        return Lists.immutable.fromStream(Arrays.stream(input.codePoints().toArray()).mapToObj(UnicodeCharacter::new));
    }

    public static @NotNull String toString(@NotNull List<UnicodeCharacter> unicodeCharacters) {
        return unicodeCharacters.stream().map(UnicodeCharacter::toString).reduce("", (a, b) -> a + b);
    }

    public static @NotNull String toUnicodeCharacter(int codePoint) {
        return Character.toString(codePoint);
    }

    public @NotNull String toString() {
        return toUnicodeCharacter(codePoint);
    }

    public static @NotNull UnicodeCharacter valueOf(int codePoint) {
        return new UnicodeCharacter(codePoint);
    }

    public static @NotNull UnicodeCharacter valueOf(String unicodeCharacter) {
        return new UnicodeCharacter(unicodeCharacter);
    }

    private UnicodeCharacter(int codePoint) {
        this(codePoint, toUnicodeCharacter(codePoint));
    }

    private UnicodeCharacter(@NotNull String unicodeCharacter) {
        this(unicodeCharacter.codePointAt(0), unicodeCharacter);
        if (unicodeCharacter.codePointCount(0, unicodeCharacter.length()) != 1) {
            throw new IllegalArgumentException(String.format("%s is not a valid unicode character", unicodeCharacter));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof UnicodeCharacter oth)
            return this.codePoint == oth.codePoint;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codePoint);
    }
}
