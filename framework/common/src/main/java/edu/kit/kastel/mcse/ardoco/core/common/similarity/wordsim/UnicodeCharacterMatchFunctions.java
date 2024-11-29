/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim;

import java.io.Serializable;
import java.util.function.BiFunction;

public enum UnicodeCharacterMatchFunctions implements BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean>, Serializable {
    EQUAL(UnicodeCharacter::equals),

    EQUAL_OR_HOMOGLYPH((a, b) -> a.equals(b) || ConfusablesHelper.areHomoglyphs(a, b));

    private final BiFunctionSerializable<UnicodeCharacter, UnicodeCharacter, Boolean> function;

    UnicodeCharacterMatchFunctions(BiFunctionSerializable<UnicodeCharacter, UnicodeCharacter, Boolean> function) {
        this.function = function;
    }

    @Override
    public Boolean apply(UnicodeCharacter unicodeCharacter, UnicodeCharacter unicodeCharacter2) {
        return function.apply(unicodeCharacter, unicodeCharacter2);
    }

    public interface BiFunctionSerializable<T, U, R> extends BiFunction<T, U, R>, Serializable {
    }
}
