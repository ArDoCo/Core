package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.MissingResourceException;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.jetbrains.annotations.NotNull;

public class ConfusablesHelper {
    private ConfusablesHelper() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    private static final MutableList<FastList<UnicodeCharacter>> homoglyphs = Lists.mutable.empty();

    private static final String confusablesSummary = "/wordsim/confusablesSummary.txt";

    private static final String separator = "	";

    static {
        parseConfusablesSummary();
    }

    protected static @NotNull FastList<UnicodeCharacter> extractHomoglyphsFromLine(String line) {
        if (!line.startsWith("#" + separator))
            return FastList.newList();

        MutableList<String> confusables = Lists.mutable.of(line.split(separator + "|\\R|\\s"));
        confusables.remove(0); //Remove leading # symbol

        //Filter because only homoglyphs are interesting
        return FastList.newList(confusables.stream()
                .filter(c -> c.codePointCount(0, c.length()) == 1)
                .mapToInt(c -> c.codePointAt(0))
                .mapToObj(UnicodeCharacter::valueOf)
                .toList());
    }

    private static void parseConfusablesSummary() {
        try (InputStream is = ConfusablesHelper.class.getResourceAsStream(confusablesSummary)) {
            if (is == null)
                throw new MissingResourceException("Could not find the resource " + confusablesSummary, File.class.getSimpleName(), confusablesSummary);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    var extracted = extractHomoglyphsFromLine(line);
                    if (!extracted.isEmpty()) {
                        homoglyphs.add(extracted);
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<UnicodeCharacter> getHomoglyphs(UnicodeCharacter unicodeCharacter) {
        return homoglyphs.stream().filter(i -> i.contains(unicodeCharacter)).flatMap(FastList::stream).toList();
    }

    public static boolean areHomoglyphs(UnicodeCharacter a, UnicodeCharacter b) {
        return a.equals(b) || getHomoglyphs(a).contains(b);
    }
}
