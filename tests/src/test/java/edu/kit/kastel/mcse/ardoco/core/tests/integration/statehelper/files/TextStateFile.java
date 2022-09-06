/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

public class TextStateFile {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));
    private static final String VALUE_SEPARATOR = "|";
    private static final String LIST_SEPARATOR = ",";

    private TextStateFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Writes out the differences of new and old results.
     *
     * @param targetFile   file to write into
     * @param arDoCoResult the ArDoCoResult
     * @throws IOException if writing fails
     */
    public static void write(Path targetFile, ArDoCoResult arDoCoResult) throws IOException {

        var builder = new StringBuilder();

        var textState = arDoCoResult.getTextState();

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(String.join(VALUE_SEPARATOR, "Reference", "Name", "Type", "SurfaceForms", "Words", "Sentences", "Claimants", LINE_SEPARATOR));
        builder.append(LINE_SEPARATOR);

        for (var nounMapping : textState.getNounMappings().toSortedListBy(NounMapping::getReference)) {

            builder.append(nounMapping.getReference());
            builder.append(VALUE_SEPARATOR);
            builder.append(df.format(nounMapping.getProbabilityForKind(MappingKind.NAME)));
            builder.append(VALUE_SEPARATOR);
            builder.append(df.format(nounMapping.getProbabilityForKind(MappingKind.TYPE)));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, nounMapping.getSurfaceForms()));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, nounMapping.getWords().collect(Word::getText).toSet().toImmutableList()));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, nounMapping.getWords()
                    .collect(Word::getSentenceNo)
                    .toSet()
                    .toSortedList()
                    .collect(no -> Integer.toString(no))));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, nounMapping.getClaimants().collect(c -> c.getClass().getSimpleName())));
            builder.append(LINE_SEPARATOR);
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static boolean writeDiff(Path sourceFile, Path targetFile, Path diffFile, ArDoCoResult arDoCoResult) throws IOException {

        var builder = new StringBuilder();

        var currentTextState = arDoCoResult.getTextState();
        var currentNounMappings = currentTextState.getNounMappings().toSortedListBy(NounMapping::getReference);

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(String.join(VALUE_SEPARATOR, "Reference", "Name", "Type", "SurfaceForms", "Words", "Sentences", "Claimants", LINE_SEPARATOR));
        builder.append(LINE_SEPARATOR);

        List<String> lines = Files.readAllLines(sourceFile);
        MutableList<String> differentNounMappings = Lists.mutable.empty();
        MutableList<String> missingNounMappings = Lists.mutable.empty();
        MutableList<String> additionalNounMappings = Lists.mutable.empty();

        int valueStart = 4;

        for (int i = valueStart; i < lines.size(); i++) {

            String line = lines.get(i);
            var parts = line.split(Pattern.quote(VALUE_SEPARATOR), -1);

            if (i - valueStart >= currentNounMappings.size()) {
                missingNounMappings.add(line);
                missingNounMappings.add(LINE_SEPARATOR);
                continue;
            }

            var currentNounMapping = currentNounMappings.get(i - valueStart);
            int order = parts[0].compareTo(currentNounMapping.getReference());

            if (order >= 0) {

                String ref = currentNounMapping.getReference();
                String nameProb = df.format(currentNounMapping.getProbabilityForKind(MappingKind.NAME));
                String typeProb = df.format(currentNounMapping.getProbabilityForKind(MappingKind.TYPE));
                String surfaceForms = String.join(LIST_SEPARATOR, currentNounMapping.getSurfaceForms());
                ImmutableList<String> words = currentNounMapping.getWords().collect(Word::getText).toSet().toImmutableList();
                ImmutableList<String> sentences = currentNounMapping.getWords()
                        .collect(Word::getSentenceNo)
                        .toSet()
                        .toSortedList()
                        .collect(no -> Integer.toString(no))
                        .toImmutable();
                ImmutableSet<String> claimants = currentNounMapping.getClaimants().collect(c -> c.getClass().getSimpleName());

                String currentLine = String.join(VALUE_SEPARATOR, ref, nameProb, typeProb, surfaceForms, String.join(LIST_SEPARATOR, words), String.join(
                        LIST_SEPARATOR, sentences), String.join(LIST_SEPARATOR, claimants));
                if (order == 0) {

                    if (!parts[1].equals(nameProb) ||//
                            !parts[2].equals(typeProb) ||//
                            !parts[3].equals(surfaceForms) ||//
                            TextStateFile.wordsNotEqual(parts[4].split(Pattern.quote(LIST_SEPARATOR), -1), words) ||//
                            TextStateFile.wordsNotEqual(parts[5].split(Pattern.quote(LIST_SEPARATOR), -1), sentences) ||//
                            TextStateFile.claimantsNotEqual(parts[6].split(Pattern.quote(LIST_SEPARATOR), -1), claimants.toList())) {

                        differentNounMappings.add(currentLine);
                        differentNounMappings.add(LINE_SEPARATOR);
                        differentNounMappings.add("instead of" + LINE_SEPARATOR);
                        differentNounMappings.add(line);
                        differentNounMappings.add(LINE_SEPARATOR);
                        differentNounMappings.add(LINE_SEPARATOR);
                    }

                } else {
                    additionalNounMappings.add(currentLine);
                    additionalNounMappings.add(LINE_SEPARATOR);
                }
            } else {
                missingNounMappings.add(line);
                missingNounMappings.add(LINE_SEPARATOR);
            }

        }

        builder.append("Different NounMappings:").append(LINE_SEPARATOR);
        differentNounMappings.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Missing NounMappings:").append(LINE_SEPARATOR);
        missingNounMappings.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Additional NounMappings:").append(LINE_SEPARATOR);
        additionalNounMappings.forEach(builder::append);
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        Files.writeString(diffFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        TextStateFile.write(targetFile, arDoCoResult);

        return missingNounMappings.size() == 0 && differentNounMappings.size() == 0 && additionalNounMappings.size() == 0;
    }

    public static boolean wordsNotEqual(String[] a1, ImmutableList<String> list2) {

        if (a1.length == 1 && a1[0].equals("") && list2.size() == 0) {
            return false;
        }

        MutableList<String> list1 = Lists.mutable.withAll(Arrays.asList(a1));
        MutableList<String> removed = Lists.mutable.empty();

        for (var element : list1) {
            if (list2.contains(element)) {
                removed.add(element);
            } else {
                if (list2.select(e -> !removed.contains(e) && e.equals(element)).size() == 1) {
                    removed.add(element);
                } else {
                    return true;
                }
            }
        }

        return removed.size() != list1.size() || removed.size() != list2.size();

    }

    public static boolean claimantsNotEqual(String[] a1, MutableList<String> list2) {

        if (a1.length == 0 && list2.size() == 0) {
            return false;
        }

        MutableList<String> list1 = Lists.mutable.withAll(Arrays.asList(a1));
        MutableList<String> removed = Lists.mutable.empty();

        for (var element : list1) {
            if (list2.contains(element)) {
                removed.add(element);
            } else {
                if (list2.select(e -> !removed.contains(e) && (e.contains(element) || element.contains(e))).size() == 1) {
                    removed.add(element);
                } else {
                    return true;
                }
            }
        }

        return removed.size() != list1.size() || removed.size() != list2.size();
    }

}
