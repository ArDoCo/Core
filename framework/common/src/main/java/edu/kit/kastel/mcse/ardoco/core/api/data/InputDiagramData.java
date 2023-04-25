/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * This {@link PipelineStepData} gives access to diagrams associated with the project to analyze.
 */
public class InputDiagramData implements PipelineStepData {
    public static final String ID = "InputDiagramData";

    private final transient String pathToDiagrams;

    /**
     * Create the data by a directory of image files.
     * 
     * @param pathToDiagrams the path to a directory containing diagrams and sketches
     */
    public InputDiagramData(String pathToDiagrams) {
        this.pathToDiagrams = Objects.requireNonNull(pathToDiagrams);
    }

    /**
     * Create the data by a directory of image files.
     *
     * @param diagramDirectory the directory containing diagrams and sketches
     */
    public InputDiagramData(File diagramDirectory) {
        this(Objects.requireNonNull(diagramDirectory).getAbsolutePath());
    }

    /**
     * Get all image files of the given directory (not recursive).
     * 
     * @return a list of image files ordered by name
     */
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
