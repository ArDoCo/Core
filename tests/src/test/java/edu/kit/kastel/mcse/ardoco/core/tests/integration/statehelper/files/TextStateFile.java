package edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
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
    public static void save(Path targetFile, ArDoCoResult arDoCoResult) throws IOException {

        var builder = new StringBuilder();

        var textState = arDoCoResult.getTextState();

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(
                "Reference" + VALUE_SEPARATOR + "Name" + VALUE_SEPARATOR + "Type" + VALUE_SEPARATOR + "SurfaceForms" + VALUE_SEPARATOR + "Words" + VALUE_SEPARATOR + "Claimants" + LINE_SEPARATOR);
        builder.append(LINE_SEPARATOR);

        for (var nounMapping : textState.getNounMappings()) {

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

}
