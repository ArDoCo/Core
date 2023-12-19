/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

/**
 * Represents a Unicode character corresponding to a particular Unicode code point. Refer to the
 * <a href="http://www.unicode.org/glossary/">Unicode Glossary</a>
 * and the Java {@link Character} documentation for an explanation of code points. Java {@link Character} instances and the corresponding primitive {@code char}
 * can not represent all unicode characters in a single instance due to historic reasons.
 */
//TODO More documentation
public final class UnicodeCharacter implements Serializable {
    private static final LinkedHashMap<Integer, UnicodeCharacter> integerToUnicode = new LinkedHashMap<>();

    private final int codePoint;

    public int getCodePoint() {
        return codePoint;
    }

    private final String representation;

    public String getRepresentation() {
        return representation;
    }

    public static ImmutableList<UnicodeCharacter> from(String input) {
        return Lists.immutable.fromStream(Arrays.stream(input.codePoints().toArray()).mapToObj(UnicodeCharacter::valueOf));
    }

    public static String toString(List<UnicodeCharacter> unicodeCharacters) {
        return unicodeCharacters.stream().map(UnicodeCharacter::toString).reduce("", (a, b) -> a + b);
    }

    public static String toUnicodeCharacter(int codePoint) {
        return Character.toString(codePoint);
    }

    public String toString() {
        return toUnicodeCharacter(codePoint);
    }

    public static UnicodeCharacter valueOf(int codePoint) {
        return integerToUnicode.computeIfAbsent(codePoint, UnicodeCharacter::new);
    }

    public static UnicodeCharacter valueOf(String representation) {
        if (representation.codePointCount(0, representation.length()) != 1) {
            throw new IllegalArgumentException(String.format("%s is not a valid unicode character", representation));
        }
        var codePoint = representation.codePointAt(0);
        return integerToUnicode.computeIfAbsent(codePoint, UnicodeCharacter::new);
    }

    private UnicodeCharacter(int codePoint) {
        this(codePoint, toUnicodeCharacter(codePoint));
    }

    private UnicodeCharacter(int codePoint, String representation) {
        this.codePoint = codePoint;
        this.representation = representation;
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
