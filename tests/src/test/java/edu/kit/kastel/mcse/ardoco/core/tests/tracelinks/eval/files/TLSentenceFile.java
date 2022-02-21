package edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.text.ISentence;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class TLSentenceFile {

    public static void saveSentences(Path targetFile, Map<Project, AgentDatastructure> dataMap) throws IOException {
        var projects = dataMap.keySet().stream().sorted().toList();
        var builder = new StringBuilder();

        for (Project project : projects) {
            ImmutableList<ISentence> sentences = dataMap.get(project).getText().getSentences();

            builder.append("# ").append(project.name()).append("\n\n");

            for (ISentence sentence : sentences) {
                builder.append("- [").append(sentence.getSentenceNumber()).append("]: ").append(sentence.getText()).append('\n');
            }

            builder.append("\n\n");
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
