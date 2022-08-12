/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TestLink;

/**
 * This is a helper class to load and write out the results of the previous evaluation run for TLR results.
 */
public class TLPreviousFile {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private TLPreviousFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Loads the previous results
     * 
     * @param sourceFile file to load from
     * @return the previous results
     * @throws IOException if file access fails
     */
    public static Collection<TLProjectEvalResult> load(Path sourceFile) throws IOException {
        List<String> lines = Files.readAllLines(sourceFile);
        Map<Project, List<TestLink>> foundLinkMap = new HashMap<>();
        List<TLProjectEvalResult> results = new ArrayList<>();

        for (String line : lines) {
            var parts = line.split(",", -1);
            Project project = Project.valueOf(parts[0]);
            String modelId = parts[1];
            int sentenceNr = Integer.parseInt(parts[2]);

            var testLink = new TestLink(modelId, sentenceNr);

            if (!foundLinkMap.containsKey(project)) {
                foundLinkMap.put(project, new ArrayList<>());
            }

            foundLinkMap.get(project).add(testLink);
        }

        for (Project project : foundLinkMap.keySet()) {
            var correctLinks = TLGoldStandardFile.loadLinks(project);
            var foundLinks = foundLinkMap.get(project);

            results.add(new TLProjectEvalResult(project, foundLinks, correctLinks));
        }

        return results;
    }

    /**
     * Saves the given results to the given file.
     * 
     * @param targetFile file to save to
     * @param results    results to save
     * @throws IOException if writing to file system fails
     */
    public static void save(Path targetFile, Collection<TLProjectEvalResult> results) throws IOException {
        if (Files.exists(targetFile)) {
            return; // do not overwrite
        }

        var sortedResults = results.stream().sorted().toList();

        var builder = new StringBuilder();

        for (TLProjectEvalResult result : sortedResults) {
            for (TestLink foundLink : result.getFoundLinks()) {
                builder.append(result.getProject().name());
                builder.append(',');
                builder.append(foundLink.modelId());
                builder.append(',');
                builder.append(foundLink.sentenceNr());
                builder.append(LINE_SEPARATOR);
            }
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE);
    }

}
