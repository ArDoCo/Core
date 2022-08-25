package edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

public class TextStateFile {
    private static final String LINE_SEPARATOR = System.lineSeparator();

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

        builder.append(
                "Reference" + VALUE_SEPARATOR + "Name" + VALUE_SEPARATOR + "Type" + VALUE_SEPARATOR + "SurfaceForms" + VALUE_SEPARATOR + "Words" + VALUE_SEPARATOR + "Claimants")
                .append(LINE_SEPARATOR);
        builder.append(LINE_SEPARATOR);

        for (var nounMapping : textState.getNounMappings().toSortedListBy(NounMapping::getReference)) {

            builder.append(nounMapping.getReference());
            builder.append(VALUE_SEPARATOR);
            builder.append(nounMapping.getProbabilityForKind(MappingKind.NAME));
            builder.append(VALUE_SEPARATOR);
            builder.append(nounMapping.getProbabilityForKind(MappingKind.TYPE));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, nounMapping.getSurfaceForms()));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, nounMapping.getWords().collect(Word::getText).toSet()));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, nounMapping.getClaimants().collect(c -> c.getClass().getSimpleName())));
            builder.append(LINE_SEPARATOR);
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void writeDiff(Path sourceFile, Path targetFile, Path diffFile, ArDoCoResult arDoCoResult) throws IOException {

        var builder = new StringBuilder();

        var currentTextState = arDoCoResult.getTextState();
        var currentNounMappings = currentTextState.getNounMappings().toSortedListBy(NounMapping::getReference);

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(
                "Reference" + VALUE_SEPARATOR + "Name" + VALUE_SEPARATOR + "Type" + VALUE_SEPARATOR + "SurfaceForms" + VALUE_SEPARATOR + "Words" + VALUE_SEPARATOR + "Claimants")
                .append(LINE_SEPARATOR);
        builder.append(LINE_SEPARATOR);

        List<String> lines = Files.readAllLines(sourceFile);
        MutableList<String> differentNounMappings = Lists.mutable.empty();
        MutableList<String> missingNounMappings = Lists.mutable.empty();
        MutableList<String> additionalNounMappings = Lists.mutable.empty();

        int valueStart = 4;

        for (int i = valueStart; i < lines.size(); i++) {

            String line = lines.get(i);
            var parts = line.split(Pattern.quote(VALUE_SEPARATOR), -1);

            var currentNounMapping = currentNounMappings.get(i - valueStart);
            int order = parts[0].compareTo(currentNounMapping.getReference());

            if (order >= 0) {

                String ref = currentNounMapping.getReference();
                String nameProb = String.valueOf(currentNounMapping.getProbabilityForKind(MappingKind.NAME));
                String typeProb = String.valueOf(currentNounMapping.getProbabilityForKind(MappingKind.TYPE));
                String surfaceForms = String.join(LIST_SEPARATOR, currentNounMapping.getSurfaceForms());
                String words = String.join(LIST_SEPARATOR, currentNounMapping.getWords().collect(Word::getText).toSet());
                String claimants = String.join(LIST_SEPARATOR, currentNounMapping.getClaimants().collect(c -> c.getClass().getSimpleName()));

                if (order == 0) {

                    if (!parts[1].equals(nameProb) ||//
                            !parts[2].equals(typeProb) ||//
                            !parts[3].equals(surfaceForms) ||//
                            !parts[4].equals(words) ||//
                            !parts[5].equals(claimants)) {

                        differentNounMappings.add(
                                ref + VALUE_SEPARATOR + nameProb + VALUE_SEPARATOR + typeProb + VALUE_SEPARATOR + surfaceForms + VALUE_SEPARATOR + words + VALUE_SEPARATOR + claimants + LINE_SEPARATOR);
                        differentNounMappings.add("instead of" + LINE_SEPARATOR);
                        differentNounMappings.add(line);
                        differentNounMappings.add(LINE_SEPARATOR);
                    }

                } else {
                    additionalNounMappings.add(
                            ref + VALUE_SEPARATOR + nameProb + VALUE_SEPARATOR + typeProb + VALUE_SEPARATOR + surfaceForms + VALUE_SEPARATOR + words + VALUE_SEPARATOR + claimants + LINE_SEPARATOR);
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
    }

}
