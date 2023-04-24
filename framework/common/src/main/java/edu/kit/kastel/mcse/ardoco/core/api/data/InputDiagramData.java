/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public class InputDiagramData implements PipelineStepData {
    public static final String ID = "InputDiagramData";

    private transient String pathToDiagrams;

    public InputDiagramData(String pathToDiagrams) {
        this.pathToDiagrams = pathToDiagrams;
    }

    public List<File> getFiles() {
        if (pathToDiagrams == null)
            return List.of();
        File directoryOfDiagrams = new File(pathToDiagrams);
        if (!directoryOfDiagrams.exists() || !directoryOfDiagrams.isDirectory()) {
            return List.of();
        }
        File[] allFiles = directoryOfDiagrams.listFiles();
        if (allFiles == null)
            return List.of();
        List<File> diagrams = Arrays.stream(allFiles)
                .filter(File::isFile)
                .filter(f -> f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".png"))
                .sorted(Comparator.comparing(File::getName))
                .toList();
        logger.info("Found {} diagrams to consider.", diagrams.size());
        return diagrams;
    }

}
