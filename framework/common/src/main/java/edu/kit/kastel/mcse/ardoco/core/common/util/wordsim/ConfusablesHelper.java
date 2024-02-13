/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Stream;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;

/**
 * This class provides functionality regarding confusables and homoglyphs of a {@link UnicodeCharacter}. The information is based on the <a
 * href="https://www.unicode.org/Public/security/latest/confusablesSummary.txt">confusablesSummary.txt</a>, that is published as part of the <a
 * href="https://www.unicode.org/reports/tr39/">Unicode Technical Standard</a>.
 */
public class ConfusablesHelper {
    private ConfusablesHelper() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    private static final LinkedHashMap<UnicodeCharacter, FastList<UnicodeCharacter>> homoglyphs = new LinkedHashMap<>();

    private static final String CONFUSABLES_SUMMARY = "/wordsim/confusablesSummary.txt";

    private static final String SEPARATOR = "\t";

    static {
        parseConfusablesSummary();
    }

    /**
     * {@return the list of homoglyphs contained in a line}
     *
     * @param line the line
     */
    static FastList<UnicodeCharacter> extractHomoglyphsFromLine(String line) {
        if (!line.startsWith("#" + SEPARATOR))
            return FastList.newList();

        MutableList<String> confusables = Lists.mutable.of(line.split("\\R|\\s"));
        confusables.remove(0); //Remove leading # symbol

        // TODO skip confusables that consist of multiple unicode characters
        // Filter because only homoglyphs are interesting
        return FastList.newList(confusables.stream()
                .filter(c -> c.codePointCount(0, c.length()) == 1)
                .mapToInt(c -> c.codePointAt(0))
                .mapToObj(UnicodeCharacter::valueOf)
                .toList());
    }

    /**
     * Parses the confusablesSummary.txt line by line and build the confusables map.
     */
    private static void parseConfusablesSummary() {
        try (InputStream is = ConfusablesHelper.class.getResourceAsStream(CONFUSABLES_SUMMARY)) {
            if (is == null)
                throw new MissingResourceException("Could not find the resource " + CONFUSABLES_SUMMARY, File.class.getSimpleName(), CONFUSABLES_SUMMARY);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    var extracted = extractHomoglyphsFromLine(line);
                    if (!extracted.isEmpty()) {
                        for (var unicodeCharacter : extracted) {
                            homoglyphs.merge(unicodeCharacter, extracted, (oldL, newL) -> FastList.newList(Stream.concat(oldL.stream(), newL.stream())
                                    .toList()));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@return the list of Unicode characters that are considered homoglyphs of the character}
     *
     * @param unicodeCharacter the character
     */
    public static List<UnicodeCharacter> getHomoglyphs(UnicodeCharacter unicodeCharacter) {
        return homoglyphs.getOrDefault(unicodeCharacter, FastList.newList());
    }

    /**
     * {@return whether two Unicode characters are considered homoglyphs} Always true for equal characters. The relationship is symmetric, but not transitive.
     *
     * @param a the first character
     * @param b the second character
     */
    public static boolean areHomoglyphs(UnicodeCharacter a, UnicodeCharacter b) {
        return a.equals(b) || getHomoglyphs(a).contains(b);
    }
}
