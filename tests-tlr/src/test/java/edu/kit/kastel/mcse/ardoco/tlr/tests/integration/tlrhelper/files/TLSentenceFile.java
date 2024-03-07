/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

/**
 * This helper-class offers functionality to write out the sentences as seen by ArDoCo after the evaluation runs for TLR are done.
 */
public class TLSentenceFile {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private TLSentenceFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Write out the sentences from the given data map to the target file
     *
     * @param targetFile file to write to
     * @param dataMap    data to extract the sentences from
     * @throws IOException if writing to file system fails
     */
    public static void save(Path targetFile, Map<GoldStandardProject, ArDoCoResult> dataMap) throws IOException {
        var projects = dataMap.keySet().stream().sorted().toList();
        var builder = new StringBuilder();

        for (GoldStandardProject project : projects) {
            ImmutableList<Sentence> sentences = dataMap.get(project).getText().getSentences();

            builder.append("# ").append(project.getProjectName());
            builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

            for (Sentence sentence : sentences) {
                builder.append("- [").append(sentence.getSentenceNumber()).append("]: ").append(sentence.getText()).append(LINE_SEPARATOR);
            }

            builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
