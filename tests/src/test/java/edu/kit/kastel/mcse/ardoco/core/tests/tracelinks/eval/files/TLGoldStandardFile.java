package edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.TestLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class TLGoldStandardFile {

    public static List<TestLink> loadLinks(Project project) throws IOException {
        Path path = Path.of(String.format("src/test/resources/%s/goldstandard.csv", project.name().toLowerCase(Locale.ROOT)));
        List<String> lines = Files.readAllLines(path);

        return lines.stream().skip(1) // skip csv header
                .map(line -> line.split(",")) // modelElementId,sentenceNr
                .map(array -> new TestLink(array[0], Integer.parseInt(array[1]))).map(link -> new TestLink(link.modelId(), link.sentenceNr() - 1))
                // ^ goldstandard sentences start with 1 while ISentences are zero indexed
                .toList();
    }

}
