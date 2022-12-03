/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new.integration.tlrhelper.files;

import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests_new.integration.tlrhelper.TestLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TLGoldStandardFile {

    private TLGoldStandardFile() {
        // no instantiation
        throw new IllegalAccessError("No instantiation allowed");
    }

    public static List<TestLink> loadLinks(Project project) throws IOException {
        Path path = project.getTlrGoldStandardFile().toPath();
        List<String> lines = Files.readAllLines(path);

        return lines.stream()
                .skip(1) // skip csv header
                .map(line -> line.split(",")) // modelElementId,sentenceNr
                .map(array -> new TestLink(array[0], Integer.parseInt(array[1])))
                .map(link -> new TestLink(link.modelId(), link.sentenceNr() - 1))
                // ^ goldstandard sentences start with 1 while ISentences are zero indexed
                .toList();
    }

}
