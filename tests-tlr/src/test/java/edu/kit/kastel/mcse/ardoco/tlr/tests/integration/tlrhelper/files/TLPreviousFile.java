/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.slf4j.Logger;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.TestLink;

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
    public static Collection<Pair<GoldStandardProject, EvaluationResults<TestLink>>> load(Path sourceFile,
            final Map<GoldStandardProject, ArDoCoResult> DATA_MAP) throws IOException {
        List<String> lines = Files.readAllLines(sourceFile);
        Map<Project, List<TestLink>> foundLinkMap = new LinkedHashMap<>();
        List<Pair<GoldStandardProject, EvaluationResults<TestLink>>> results = new ArrayList<>();

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

            ArDoCoResult arDoCoResult = DATA_MAP.get(project);
            if (arDoCoResult != null) {
                results.add(Tuples.pair(project, TestUtil.compareTLR(arDoCoResult, Lists.immutable.ofAll(foundLinks), correctLinks.toImmutable())));
            }
        }

        return results;
    }

    /**
     * Saves the given results to the given file.
     *
     * @param targetFile     file to save to
     * @param projectResults results to save
     * @throws IOException if writing to file system fails
     */
    public static void save(Path targetFile, Collection<Pair<GoldStandardProject, EvaluationResults<TestLink>>> projectResults, Logger logger)
            throws IOException {
        if (Files.exists(targetFile)) {
            logger.warn("File with the results of the previous evaluation run already exists.");
            return; // do not overwrite
        }

        var sortedResults = new ArrayList<>(projectResults);
        sortedResults.sort(Comparator.comparing(x -> x.getOne().getProjectName()));

        var builder = new StringBuilder();

        for (Pair<GoldStandardProject, EvaluationResults<TestLink>> projectResult : sortedResults) {
            EvaluationResults<TestLink> result = projectResult.getTwo();
            for (TestLink foundLink : result.getFound()) {
                builder.append(projectResult.getOne().getProjectName());
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
